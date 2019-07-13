package com.gitlab.lae.intellij.actions.tree;

import com.google.auto.value.AutoValue;
import com.intellij.ide.DataManager;
import com.intellij.ide.IdeEventQueue;
import com.intellij.ide.IdePopupManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.ui.components.JBList;
import com.intellij.util.Consumer;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.function.Supplier;

import static com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR;
import static com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_ESCAPE;
import static com.intellij.openapi.actionSystem.PlatformDataKeys.CONTEXT_COMPONENT;
import static com.intellij.openapi.actionSystem.ex.ActionUtil.*;
import static java.util.stream.Collectors.toList;

@AutoValue
abstract class ActionNode {
    ActionNode() {
    }

    // Use ACTION_SEARCH as the action place seems to work the best.
    //
    // 'Run | Stop' menu action works correctly this way by
    // showing a list of processes to stop
    //
    // 'Exit' actions works (doesn't work if place is MAIN_MENU)
    //
    private static final String ACTION_PLACE =
            ActionPlaces.ACTION_SEARCH;

    static ActionNode create(
            String id,
            String name,
            String separatorAbove,
            boolean sticky,
            List<KeyStroke> keys,
            List<ActionNode> items
    ) {
        return new AutoValue_ActionNode(
                id,
                name,
                separatorAbove,
                sticky,
                keys,
                items
        );
    }

    abstract String id();

    @Nullable
    abstract String name();

    @Nullable
    abstract String separatorAbove();

    abstract boolean sticky();

    abstract List<KeyStroke> keys();

    abstract List<ActionNode> items();

    private ActionPresentation toPresentation(AnActionEvent e) {
        AnAction action = toAction(this, e.getActionManager());
        Presentation presentation = action.getTemplatePresentation().clone();
        AnActionEvent event = new AnActionEvent(
                null,
                e.getDataContext(),
                ACTION_PLACE,
                presentation,
                e.getActionManager(),
                e.getModifiers()
        );
        event.setInjectedContext(action.isInInjectedContext());

        performDumbAwareUpdate(true, action, event, false);
        return ActionPresentation.create(
                presentation,
                keys(),
                separatorAbove(),
                sticky(),
                action
        );
    }

    static AnAction toAction(ActionNode node, ActionManager mgr) {
        if (node.items().isEmpty()) {
            AnAction action = mgr.getAction(node.id());
            if (action != null) {
                return action;
            }
            return new AnAction("?" + node.id() + "?") {
                @Override
                public void actionPerformed(AnActionEvent e) {
                }

                @Override
                public void update(AnActionEvent e) {
                    e.getPresentation().setEnabled(false);
                }
            };
        }
        return new AnAction(node.name()) {
            @Override
            public boolean isDumbAware() {
                return true;
            }

            @Override
            public void actionPerformed(AnActionEvent e) {
                node.showPopup(e);
            }
        };
    }

    private void showPopup(AnActionEvent e) {
        Component component = e.getData(CONTEXT_COMPONENT);
        List<ActionPresentation> presentations = items().stream()
                .map(it -> it.toPresentation(e))
                .collect(toList());

        JBPopup[] popup = {null};
        ActionList<ActionPresentation> list = new ActionList<>(presentations);
        list.setCellRenderer(new ActionPresentationRenderer());

        // Register our action first before IntelliJ registers the default
        // actions (e.g. com.intellij.ui.ScrollingUtil) so that in case of
        // conflict our action will be executed
        registerKeys(list, component, e.getData(EDITOR), () -> popup[0]);
        registerIdeAction(list, ACTION_EDITOR_ESCAPE, e.getActionManager(),
                () -> {
                    if (popup[0] != null) popup[0].cancel();
                });

        popup[0] = JBPopupFactory.getInstance()
                .createListPopupBuilder(list)
                .setModalContext(true)
                .setItemChoosenCallback(() -> {
                    ActionPresentation value = list.getSelectedValue();
                    if (value != null && value.presentation().isEnabled()) {
                        performAction(value.action(), component, 0);
                    }
                })
                .createPopup();

        ActionDispatcher dispatcher = new ActionDispatcher(popup[0], list);
        popup[0].addListener(new JBPopupListener() {
            IdePopupManager pm = IdeEventQueue.getInstance().getPopupManager();

            @Override
            public void beforeShown(LightweightWindowEvent event) {
                pm.push(dispatcher);
            }

            @Override
            public void onClosed(LightweightWindowEvent event) {
                pm.remove(dispatcher);
            }
        });
        popup[0].showInBestPositionFor(e.getDataContext());
    }

    private void registerKeys(
            JList<ActionPresentation> list,
            Component component,
            Editor editor,
            Supplier<JBPopup> getPopup
    ) {

        for (int i = 0; i < list.getModel().getSize(); i++) {
            ActionPresentation item = list.getModel().getElementAt(i);
            if (item.keys().isEmpty()) {
                continue;
            }

            InputMap inputMap = list.getInputMap();
            ActionMap actionMap = list.getActionMap();
            for (KeyStroke key : item.keys()) {
                int index = i;
                inputMap.put(key, key);
                actionMap.put(key, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        if (!item.presentation().isEnabled()) return;
                        JBPopup popup = getPopup.get();
                        if (popup == null) {
                            return;
                        }
                        list.setSelectedIndex(index);

                        Runnable runnable = () -> performAction(
                                item.action(), component, e.getModifiers());

                        if (!item.sticky()) {
                            popup.setFinalRunnable(runnable);
                            popup.closeOk(null);
                            return;
                        }

                        runnable.run();
                        if (editor != null) {
                            editor.getScrollingModel().runActionOnScrollingFinished(() ->
                                    popup.setLocation(JBPopupFactory.getInstance()
                                            .guessBestPopupLocation(editor)
                                            .getScreenPoint()));
                        } else if (component instanceof JComponent) {
                            popup.setLocation(JBPopupFactory.getInstance()
                                    .guessBestPopupLocation((JComponent) component)
                                    .getScreenPoint());
                        }
                    }
                });
            }
        }
    }

    private void registerIdeAction(
            JBList<ActionPresentation> list,
            String actionId,
            ActionManager actionManager,
            Runnable runnable
    ) {
        AnAction action = actionManager.getAction(actionId);
        if (action == null) {
            return;
        }
        ShortcutSet shortcutSet = action.getShortcutSet();
        if (shortcutSet.getShortcuts().length == 0) {
            return;
        }
        new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent e) {
                runnable.run();
            }
        }.registerCustomShortcutSet(shortcutSet, list);
    }

    private void performAction(AnAction action, Component component, int modifiers) {
        DataManager dataManager = DataManager.getInstance();
        (component == null
                ? dataManager.getDataContextFromFocus()
                : AsyncResult.done(dataManager.getDataContext(component)))
                .doWhenDone((Consumer<DataContext>) dataContext ->
                        performAction(action, dataContext, modifiers));
    }

    private void performAction(
            AnAction action,
            DataContext dataContext,
            int modifiers
    ) {
        ActionManager actions = ActionManager.getInstance();
        Presentation presentation = action.getTemplatePresentation().clone();
        AnActionEvent event = new AnActionEvent(
                null,
                dataContext,
                ACTION_PLACE,
                presentation,
                actions,
                modifiers
        );
        event.setInjectedContext(action.isInInjectedContext());

        if (showPopupIfGroup(action, event)) return;
        if (lastUpdateAndCheckDumb(action, event, false)) {
            performActionDumbAware(action, event);
        }
    }

    private boolean showPopupIfGroup(AnAction action, AnActionEvent e) {
        if (!(action instanceof ActionGroup)) {
            return false;
        }
        ActionGroup group = (ActionGroup) action;
        if (group.canBePerformed(e.getDataContext())) {
            return false;
        }
        JBPopupFactory.getInstance()
                .createActionGroupPopup(
                        e.getPresentation().getText(),
                        group,
                        e.getDataContext(),
                        JBPopupFactory.ActionSelectionAid.NUMBERING,
                        true
                )
                .showInBestPositionFor(e.getDataContext());
        return true;
    }
}
