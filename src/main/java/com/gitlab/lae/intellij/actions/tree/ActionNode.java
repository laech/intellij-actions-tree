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
import com.intellij.openapi.util.Pair;

import javax.annotation.Nullable;
import javax.swing.*;
import java.util.*;

import static java.util.stream.Collectors.toList;

@AutoValue
public abstract class ActionNode {
    ActionNode() {
    }

    public static ActionNode create(
            String id,
            String name,
            String separatorAbove,
            boolean sticky,
            When when,
            List<KeyStroke> keys,
            List<ActionNode> items
    ) {
        return new AutoValue_ActionNode(
                id,
                name,
                separatorAbove,
                sticky,
                when,
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

    public abstract When when();

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
            DataManager dataManager,
            List<KeyStroke> keysOverride
    ) {
        AnAction action = toAction(
                actionManager,
                popupManager,
                popupFactory,
                dataManager
        );
        ActionPresentation presentation = ActionPresentation.create(
                action,
                keysOverride,
                separatorAbove(),
                sticky()
        );
        presentation.update(actionManager, dataContext);
        return presentation;
    }

    public AnAction toAction(
            ActionManager actionManager,
            IdePopupManager popupManager,
            JBPopupFactory popupFactory,
            DataManager dataManager
    ) {
        if (items().isEmpty()) {
            AnAction action = actionManager.getAction(id());
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

    public List<Pair<List<KeyStroke>, ActionNode>> prepare(DataContext context) {
        Set<KeyStroke> registered = new HashSet<>();
        List<Pair<List<KeyStroke>, ActionNode>> result = new ArrayList<>();
        List<ActionNode> items = new ArrayList<>(items());
        Collections.reverse(items);

        for (ActionNode item : items) {
            if (!item.when().test(context)) {
                continue;
            }

            List<KeyStroke> keys = item.keys()
                    .stream()
                    .filter(registered::add)
                    .collect(toList());

            if (keys.isEmpty()) {
                continue;
            }

            result.add(Pair.create(keys, item));
        }

        Collections.reverse(result);
        return result;
    }

}
