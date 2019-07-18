package com.gitlab.lae.intellij.actions.tree.util;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.util.Consumer;

import java.awt.*;

import static com.intellij.openapi.actionSystem.ex.ActionUtil.lastUpdateAndCheckDumb;
import static com.intellij.openapi.actionSystem.ex.ActionUtil.performActionDumbAware;
import static com.intellij.openapi.ui.popup.JBPopupFactory.ActionSelectionAid.NUMBERING;

public final class Actions {
    private Actions() {
    }

    /* Use ACTION_SEARCH as the action place seems to work the best.
     *
     * 'Run | Stop' menu action works correctly this way by
     * showing a list of processes to stop
     *
     * 'Exit' actions works (doesn't work if place is MAIN_MENU)
     */
    public static final String ACTION_PLACE =
            ActionPlaces.ACTION_SEARCH;

    public static void performAction(
            AnAction action,
            int modifiers,
            ActionManager actionManager,
            JBPopupFactory popupFactory,
            DataManager dataManager,
            Component component
    ) {
        (component == null
                ? dataManager.getDataContextFromFocus()
                : AsyncResult.done(dataManager.getDataContext(component))
        ).doWhenDone((Consumer<DataContext>) dataContext -> performAction(
                action,
                modifiers,
                actionManager,
                popupFactory,
                dataContext
        ));
    }

    public static void performAction(
            AnAction action,
            int modifiers,
            ActionManager actionManager,
            JBPopupFactory popupFactory,
            DataContext dataContext
    ) {
        Presentation presentation = action.getTemplatePresentation().clone();
        AnActionEvent event = new AnActionEvent(
                null,
                dataContext,
                ACTION_PLACE,
                presentation,
                actionManager,
                modifiers
        );
        event.setInjectedContext(action.isInInjectedContext());

        if (showPopupIfGroup(action, event, popupFactory)) {
            return;
        }
        if (lastUpdateAndCheckDumb(action, event, false)) {
            performActionDumbAware(action, event);
        }
    }

    private static boolean showPopupIfGroup(
            AnAction action,
            AnActionEvent e,
            JBPopupFactory popupFactory
    ) {
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
