package com.gitlab.lae.intellij.actions.tree;

import com.gitlab.lae.intellij.actions.tree.ui.ActionPresentation;
import com.google.auto.value.AutoValue;
import com.intellij.ide.DataManager;
import com.intellij.ide.IdePopupManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.ui.popup.JBPopupFactory;

import javax.annotation.Nullable;
import javax.swing.*;
import java.util.List;

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
    public static final String ACTION_PLACE =
            ActionPlaces.ACTION_SEARCH;

    ActionPresentation createPresentation(
            ActionManager actionManager,
            DataContext dataContext,
            IdePopupManager popupManager,
            JBPopupFactory popupFactory,
            DataManager dataManager
    ) {
        AnAction action = toAction(
                actionManager,
                popupManager,
                popupFactory,
                dataManager,
                false
        );
        ActionPresentation presentation = ActionPresentation.create(
                action,
                keys(),
                separatorAbove(),
                sticky()
        );
        presentation.update(actionManager, dataContext);
        return presentation;
    }

    public AnAction toAction(
            ActionManager mgr,
            IdePopupManager popupManager,
            JBPopupFactory popupFactory,
            DataManager dataManager,
            boolean wrapIdeAction
    ) {
        if (items().isEmpty()) {
            AnAction action = mgr.getAction(id());
            return action != null
                    ? (wrapIdeAction ? new RefAction(action) : action)
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
