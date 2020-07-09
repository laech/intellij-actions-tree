package com.gitlab.lae.intellij.actions.tree.app;

import com.gitlab.lae.intellij.actions.tree.ActionNode;
import com.gitlab.lae.intellij.actions.tree.When;
import com.gitlab.lae.intellij.actions.tree.util.Actions;
import com.google.common.collect.ImmutableList;
import com.intellij.ide.DataManager;
import com.intellij.ide.IdePopupManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.IdeFocusManager;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;
import java.util.Map.Entry;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.*;

final class RootAction extends AnAction {

    private final String id;
    private final List<Pair<AnAction, When>> actions;

    RootAction(
            String id,
            List<KeyStroke> keyStrokes,
            List<Pair<AnAction, When>> actions
    ) {
        super(actions.stream()
                .map(pair -> pair.getFirst().getTemplatePresentation().getText())
                .collect(joining(", ")));

        this.id = requireNonNull(id);
        this.actions = ImmutableList.copyOf(actions);
        super.setShortcutSet(new CustomShortcutSet(keyStrokes.stream()
                .map(it -> new KeyboardShortcut(it, null))
                .toArray(Shortcut[]::new)));

        setEnabledInModalContext(actions.stream()
                .anyMatch(it -> it.getFirst().isEnabledInModalContext()));
    }

    @Override
    protected void setShortcutSet(ShortcutSet ignored) {
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, actions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RootAction action = (RootAction) o;
        return Objects.equals(id, action.id) &&
                Objects.equals(actions, action.actions);
    }

    @Override
    public String toString() {
        return "RootAction{" +
                "id='" + id + '\'' +
                ", actions=" + actions +
                '}';
    }

    String getId() {
        return id;
    }

    @NotNull
    private Optional<AnAction> findAction(AnActionEvent e) {
        return actions.stream()
                .filter(it -> it.getSecond().test(e.getDataContext()))
                .findAny()
                .map(Pair<AnAction, When>::getFirst);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        findAction(e).ifPresent(it -> it.actionPerformed(e));
    }

    @Override
    public void beforeActionPerformedUpdate(AnActionEvent e) {
        super.beforeActionPerformedUpdate(e);
        findAction(e).ifPresent(it -> it.beforeActionPerformedUpdate(e));
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);

        /* It's important to disable this action when none inner action
         * is available to execute, so that the key stroke will be passed
         * through to the IDE for processing.
         *
         * For example, if the 'N' key is configured to be the 'Down'
         * action when the Project tool window is active, it should do
         * that when the Project tool window is active, but when the editor
         * is active, it should still insert the 'N' character, because
         * the 'N' key is not bounded to any action when the editor is
         * active, disabling the action correctly allows this behaviour.
         */
        Optional<AnAction> action = findAction(e);
        e.getPresentation().setEnabled(action.isPresent());
        action.ifPresent(it -> {
            it.update(e);
            Actions.setEnabledModalContext(e, it);
        });
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }

    /**
     * Merges the root level actions by their key strokes.
     * <p>
     * For example:
     * <pre>
     * ActionNode{keyStrokes=["ctrl X"], id=1 ...}
     * ActionNode{keyStrokes=["ctrl X", "ctrl Y"], id=2 ...}
     * ActionNode{keyStrokes=["ctrl A", "ctrl B"], id=3 ...}
     * </pre>
     * Becomes:
     * <pre>
     * RootAction{keyStrokes=["ctrl X"], actions=[{id=2, ...}, {id=1, ...}]}
     * RootAction{keyStrokes=["ctrl Y"], actions=[{id=2, ...}]}
     * RootAction{keyStrokes=["ctrl A", "ctrl B"], actions=[{id=3, ...}]}
     * </pre>
     * The order of the inner actions of each {@link RootAction} is reverse.
     */
    static List<RootAction> merge(
            Collection<ActionNode> actions,
            ActionManager actionManager,
            IdeFocusManager focusManager,
            IdePopupManager popupManager,
            JBPopupFactory popupFactory,
            DataManager dataManager
    ) {

        List<ActionNode> noKeys = new ArrayList<>(0);
        Map<KeyStroke, List<ActionNode>> byKeys = new LinkedHashMap<>();
        for (ActionNode action : actions) {
            List<KeyStroke> keys = action.getKeys();
            if (keys.isEmpty()) {
                noKeys.add(action);
                continue;
            }
            for (KeyStroke key : keys) {
                byKeys.computeIfAbsent(key, __ -> new ArrayList<>(1))
                        .add(action);
            }
        }

        Map<List<ActionNode>, List<KeyStroke>> byActions =
                new LinkedHashMap<>();
        for (Entry<KeyStroke, List<ActionNode>> entry : byKeys.entrySet()) {
            byActions
                    .computeIfAbsent(entry.getValue(), __ -> new ArrayList<>(1))
                    .add(entry.getKey());
        }
        if (!noKeys.isEmpty()) {
            byActions.put(noKeys, emptyList());
        }

        int counter = 0;
        List<RootAction> result = new ArrayList<>();
        for (Entry<List<ActionNode>, List<KeyStroke>> entry :
                byActions.entrySet()) {

            List<KeyStroke> keys = entry.getValue();
            List<ActionNode> nodes = entry.getKey();

            /*
             * If list contains a single action group, use that group's
             * ID as the action ID, this allows user specified IDs to be
             * maintained and referenced in other plugins such as IdeaVim's
             * :action command if they wish.
             *
             * We only do this for custom action groups and not for references
             * to existing action items because it will result in duplicate
             * ID error when registering later.
             */
            String id = nodes.size() == 1 && !nodes.get(0).getItems().isEmpty()
                    ? nodes.get(0).getId()
                    : "ActionsTree.Root." + counter;

            result.add(new RootAction(id, keys, nodes.stream()
                    .map(it -> new Pair<>(
                            it.toAction(
                                    actionManager,
                                    focusManager,
                                    popupManager,
                                    popupFactory,
                                    dataManager
                            ),
                            it.getCondition()
                    ))
                    .collect(collectingAndThen(
                            toCollection(ArrayList::new),
                            list -> {
                                Collections.reverse(list);
                                return list;
                            }
                    ))
            ));

            counter++;
        }
        return result;
    }
}
