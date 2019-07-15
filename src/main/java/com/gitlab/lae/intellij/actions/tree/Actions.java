package com.gitlab.lae.intellij.actions.tree;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.util.Consumer;

import java.awt.*;

import static com.intellij.openapi.actionSystem.ex.ActionUtil.lastUpdateAndCheckDumb;
import static com.intellij.openapi.actionSystem.ex.ActionUtil.performActionDumbAware;
import static com.intellij.openapi.ui.popup.JBPopupFactory.ActionSelectionAid.NUMBERING;

final class Actions {
    private Actions() {
    }

    /* Use ACTION_SEARCH as the action place seems to work the best.
     *
     * 'Run | Stop' menu action works correctly this way by
     * showing a list of processes to stop
     *
     * 'Exit' actions works (doesn't work if place is MAIN_MENU)
     */
    static final String ACTION_PLACE =
            ActionPlaces.ACTION_SEARCH;

    static void performAction(
            AnAction action,
            Component component,
            int modifiers
    ) {
        DataManager dataManager = DataManager.getInstance();
        (component == null
                ? dataManager.getDataContextFromFocus()
                : AsyncResult.done(dataManager.getDataContext(component)))
                .doWhenDone((Consumer<DataContext>) dataContext ->
                        performAction(action, dataContext, modifiers));
    }

    static void performAction(
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

    private static boolean showPopupIfGroup(AnAction action, AnActionEvent e) {
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
                        NUMBERING,
                        true
                )
                .showInBestPositionFor(e.getDataContext());
        return true;
    }
}
