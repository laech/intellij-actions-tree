package com.gitlab.lae.intellij.actions.tree;

import com.gitlab.lae.intellij.actions.tree.ui.ActionList;
import com.gitlab.lae.intellij.actions.tree.ui.ActionPopupEventDispatcher;
import com.gitlab.lae.intellij.actions.tree.ui.ActionPresentation;
import com.intellij.ide.DataManager;
import com.intellij.ide.IdePopupManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.util.Consumer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

import static com.gitlab.lae.intellij.actions.tree.util.JBPopups.setBestLocation;
import static com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR;
import static com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_ESCAPE;
import static com.intellij.openapi.actionSystem.PlatformDataKeys.CONTEXT_COMPONENT;
import static com.intellij.openapi.actionSystem.ex.ActionUtil.lastUpdateAndCheckDumb;
import static com.intellij.openapi.actionSystem.ex.ActionUtil.performActionDumbAware;
import static com.intellij.openapi.ui.popup.JBPopupFactory.ActionSelectionAid.NUMBERING;
import static com.intellij.openapi.util.AsyncResult.done;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

final class Popup {

    private final ActionManager actionManager;
    private final Component sourceComponent;
    private final Editor sourceEditor;
    private final IdePopupManager popupManager;
    private final JBPopupFactory popupFactory;
    private final DataManager dataManager;
    private final ActionList list;
    private final JBPopup popup;

    Popup(
            ActionNode action,
            AnActionEvent e,
            IdePopupManager popupManager,
            JBPopupFactory popupFactory,
            DataManager dataManager
    ) {
        this.popupManager = requireNonNull(popupManager);
        this.popupFactory = requireNonNull(popupFactory);
        this.dataManager = requireNonNull(dataManager);
        this.actionManager = e.getActionManager();
        this.sourceComponent = e.getData(CONTEXT_COMPONENT);
        this.sourceEditor = e.getData(EDITOR);

        List<ActionPresentation> items = action.items().stream()
                .map(it -> createPresentation(it, e))
                .collect(toList());

        list = new ActionList(items);

        // Register our action first before IntelliJ registers the default
        // actions (e.g. com.intellij.ui.ScrollingUtil) so that in case of
        // conflict our action will be executed
        items.forEach(item -> item.registerShortcuts(
                list,
                this::onActionChosen
        ));

        popup = createPopup();
        popup.addListener(new ActionPopupEventDispatcher(
                popup,
                list,
                popupManager
        ));

        registerIdeAction(ACTION_EDITOR_ESCAPE, popup::cancel);
    }

    void show(DataContext dataContext) {
        popup.showInBestPositionFor(dataContext);
    }

    private ActionPresentation createPresentation(
            ActionNode action,
            AnActionEvent event
    ) {
        return action.createPresentation(
                event,
                popupManager,
                popupFactory,
                dataManager
        );
    }

    private JBPopup createPopup() {
        return popupFactory
                .createListPopupBuilder(list)
                .setModalContext(true)
                .setItemChoosenCallback(() -> {
                    ActionPresentation item = list.getSelectedValue();
                    if (item != null) {
                        onActionChosen(item, 0);
                    }
                })
                .createPopup();
    }

    private void registerIdeAction(
            String actionId,
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

    private void onActionChosen(ActionPresentation item, ActionEvent e) {
        onActionChosen(item, e.getModifiers());
    }

    private void onActionChosen(ActionPresentation item, int modifiers) {
        if (!item.presentation().isEnabled()) {
            return;
        }

        list.setSelectedValue(item, false);

        Runnable invocation = () -> performAction(item.action(), modifiers);

        if (item.sticky()) {
            invocation.run();
            updatePopupLocation();
        } else {
            popup.setFinalRunnable(invocation);
            popup.closeOk(null);
        }
    }

    private void updatePopupLocation() {
        if (sourceEditor != null) {
            setBestLocation(popup, popupFactory, sourceEditor);
        } else if (sourceComponent instanceof JComponent) {
            setBestLocation(
                    popup,
                    popupFactory,
                    (JComponent) sourceComponent
            );
        }
    }

    /* Use ACTION_SEARCH as the action place seems to work the best.
     *
     * 'Run | Stop' menu action works correctly this way by
     * showing a list of processes to stop
     *
     * 'Exit' actions works (doesn't work if place is MAIN_MENU)
     */
    private static final String ACTION_PLACE =
            ActionPlaces.ACTION_SEARCH;

    private void performAction(AnAction action, int modifiers) {
        (sourceComponent == null
                ? dataManager.getDataContextFromFocus()
                : done(dataManager.getDataContext(sourceComponent))
        ).doWhenDone((Consumer<DataContext>) dataContext -> performAction(
                action,
                modifiers,
                dataContext
        ));
    }

    private void performAction(
            AnAction action,
            int modifiers,
            DataContext dataContext
    ) {
        AnActionEvent event = new AnActionEvent(
                null,
                dataContext,
                ACTION_PLACE,
                action.getTemplatePresentation().clone(),
                actionManager,
                modifiers
        );
        event.setInjectedContext(action.isInInjectedContext());

        if (showPopupIfGroup(action, event)) {
            return;
        }
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
        popupFactory.createActionGroupPopup(
                e.getPresentation().getText(),
                group,
                e.getDataContext(),
                NUMBERING,
                true
        ).showInBestPositionFor(e.getDataContext());
        return true;
    }
}
