package com.gitlab.lae.intellij.actions.tree;

import com.gitlab.lae.intellij.actions.tree.ui.ActionPresentation;
import com.google.auto.value.AutoValue;
import com.intellij.ide.DataManager;
import com.intellij.ide.IdePopupManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ui.popup.JBPopupFactory;

import javax.annotation.Nullable;
import javax.swing.*;
import java.util.List;

import static com.intellij.openapi.actionSystem.ex.ActionUtil.performDumbAwareUpdate;

@AutoValue
public abstract class ActionNode {
    ActionNode() {
    }

    public static ActionNode create(
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

    public abstract String id();

    @Nullable
    abstract String name();

    @Nullable
    abstract String separatorAbove();

    abstract boolean sticky();

    public abstract List<KeyStroke> keys();

    public abstract List<ActionNode> items();

    /* Use ACTION_SEARCH as the action place seems to work the best.
     *
     * 'Run | Stop' menu action works correctly this way by
     * showing a list of processes to stop
     *
     * 'Exit' actions works (doesn't work if place is MAIN_MENU)
     */
    static final String ACTION_PLACE =
            ActionPlaces.ACTION_SEARCH;

    ActionPresentation createPresentation(
            AnActionEvent e,
            IdePopupManager popupManager,
            JBPopupFactory popupFactory,
            DataManager dataManager
    ) {
        AnAction action = toAction(
                e.getActionManager(),
                popupManager,
                popupFactory,
                dataManager
        );
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

    public AnAction toAction(
            ActionManager mgr,
            IdePopupManager popupManager,
            JBPopupFactory popupFactory,
            DataManager dataManager
    ) {
        if (items().isEmpty()) {
            AnAction action = mgr.getAction(id());
            return action != null
                    ? action
                    : new UnknownAction(this);
        }
        return new PopupAction(
                this,
                popupManager,
                popupFactory,
                dataManager
        );
    }

}
