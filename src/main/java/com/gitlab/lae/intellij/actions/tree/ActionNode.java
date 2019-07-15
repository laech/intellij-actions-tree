package com.gitlab.lae.intellij.actions.tree;

import com.google.auto.value.AutoValue;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.components.JBList;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.function.Supplier;

import static com.gitlab.lae.intellij.actions.tree.Actions.ACTION_PLACE;
import static com.gitlab.lae.intellij.actions.tree.Actions.performAction;
import static com.gitlab.lae.intellij.actions.tree.JBPopups.setBestLocation;
import static com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR;
import static com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_ESCAPE;
import static com.intellij.openapi.actionSystem.PlatformDataKeys.CONTEXT_COMPONENT;
import static com.intellij.openapi.actionSystem.ex.ActionUtil.performDumbAwareUpdate;
import static java.util.stream.Collectors.toList;

@AutoValue
abstract class ActionNode {
    ActionNode() {
    }

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
        AnAction action = toAction(e.getActionManager());
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

    AnAction toAction(ActionManager mgr) {
        if (!items().isEmpty()) {
            return toPopupAction();
        }
        AnAction action = mgr.getAction(id());
        if (action != null) {
            action = toUnknownAction();
        }
        return action;
    }

    private AnAction toUnknownAction() {
        return new AnAction("?" + id() + "?") {

            @Override
            public void actionPerformed(AnActionEvent e) {
            }

            @Override
            public void update(AnActionEvent e) {
                e.getPresentation().setEnabled(false);
            }
        };
    }

    private AnAction toPopupAction() {
        return new AnAction(name()) {

            @Override
            public boolean isDumbAware() {
                return true;
            }

            @Override
            public void actionPerformed(AnActionEvent e) {
                showPopup(e);
            }
        };
    }

    private void showPopup(AnActionEvent e) {
        Component component = e.getData(CONTEXT_COMPONENT);
        List<ActionPresentation> presentations = items().stream()
                .map(it -> it.toPresentation(e))
                .collect(toList());

        JBPopup[] popupHolder = {null};
        ActionList<ActionPresentation> list = new ActionList<>(presentations);
        list.setCellRenderer(new ActionPresentationRenderer());

        // Register our action first before IntelliJ registers the default
        // actions (e.g. com.intellij.ui.ScrollingUtil) so that in case of
        // conflict our action will be executed
        registerKeys(list, component, e.getData(EDITOR), () -> popupHolder[0]);
        registerIdeAction(list, ACTION_EDITOR_ESCAPE, e.getActionManager(),
                () -> {
                    JBPopup popup = popupHolder[0];
                    if (popup != null) {
                        popup.cancel();
                    }
                }
        );

        JBPopup popup = createPopup(component, list);
        popup.addListener(new ActionPopupEventDispatcher(popup, list));
        popup.showInBestPositionFor(e.getDataContext());
        popupHolder[0] = popup;
    }

    private JBPopup createPopup(
            Component component,
            ActionList<ActionPresentation> list
    ) {
        return JBPopupFactory.getInstance()
                .createListPopupBuilder(list)
                .setModalContext(true)
                .setItemChoosenCallback(() -> {
                    ActionPresentation value = list.getSelectedValue();
                    if (value != null && value.presentation().isEnabled()) {
                        performAction(value.action(), component, 0);
                    }
                })
                .createPopup();
    }

    private void registerKeys(
            JList<ActionPresentation> list,
            Component component,
            Editor editor,
            Supplier<JBPopup> getPopup
    ) {
        ListModels.stream(list.getModel())
                .filter(it -> !it.keys().isEmpty())
                .forEach(item -> registerKeys(
                        list,
                        component,
                        editor,
                        getPopup,
                        item
                ));
    }

    private void registerKeys(
            JList<ActionPresentation> list,
            Component component,
            Editor editor,
            Supplier<JBPopup> getPopup,
            ActionPresentation item
    ) {
        InputMap inputMap = list.getInputMap();
        ActionMap actionMap = list.getActionMap();
        for (KeyStroke key : item.keys()) {
            inputMap.put(key, key);
            actionMap.put(
                    key,
                    new InvokeAction(item, getPopup, list, component, editor)
            );
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

    private static class InvokeAction extends AbstractAction {
        private final ActionPresentation item;
        private final Supplier<JBPopup> getPopup;
        private final JList<ActionPresentation> list;
        private final Component component;
        private final Editor editor;

        InvokeAction(
                ActionPresentation item,
                Supplier<JBPopup> getPopup,
                JList<ActionPresentation> list,
                Component component,
                Editor editor
        ) {
            this.item = item;
            this.getPopup = getPopup;
            this.list = list;
            this.component = component;
            this.editor = editor;
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            if (!item.presentation().isEnabled()) {
                return;
            }

            JBPopup popup = getPopup.get();
            if (popup == null) {
                return;
            }

            list.setSelectedValue(item, false);

            Runnable runnable = () -> performAction(
                    item.action(),
                    component,
                    e.getModifiers()
            );

            if (!item.sticky()) {
                popup.setFinalRunnable(runnable);
                popup.closeOk(null);
                return;
            }

            runnable.run();
            if (editor != null) {
                setBestLocation(popup, editor);
            } else if (component instanceof JComponent) {
                setBestLocation(popup, (JComponent) component);
            }
        }
    }
}
