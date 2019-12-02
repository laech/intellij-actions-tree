package com.gitlab.lae.intellij.actions.tree.app;

import com.gitlab.lae.intellij.actions.tree.ActionNode;
import com.gitlab.lae.intellij.actions.tree.When;
import com.google.common.collect.ImmutableList;
import com.intellij.ide.DataManager;
import com.intellij.ide.IdePopupManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;
import java.util.Map.Entry;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

final class RootAction extends AnAction {

    private final String id;
    private final List<Pair<AnAction, When>> actions;

    RootAction(
            String id,
            List<KeyStroke> keyStrokes,
            List<Pair<AnAction, When>> actions
    ) {
        super(actions.stream()
                .map(pair -> pair.first.getTemplatePresentation().getText())
                .collect(joining(", ")));
        this.id = requireNonNull(id);
        this.actions = ImmutableList.copyOf(actions);
        super.setShortcutSet(new CustomShortcutSet(keyStrokes.stream()
                .map(it -> new KeyboardShortcut(it, null))
                .toArray(Shortcut[]::new)));
    }

    @Override
    protected void setShortcutSet(ShortcutSet ignored) {
    }

    String getId() {
        return id;
    }

    @NotNull
    private Optional<AnAction> findAction(AnActionEvent e) {
        return actions.stream()
                .filter(it -> it.second.test(e.getDataContext()))
                .findAny()
                .map(it -> it.first);
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
        findAction(e).ifPresent(it -> it.update(e));
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
            IdePopupManager popupManager,
            JBPopupFactory popupFactory,
            DataManager dataManager
    ) {

        Map<KeyStroke, List<ActionNode>> byKeys = new HashMap<>();
        for (ActionNode action : actions) {
            for (KeyStroke key : action.keys()) {
                byKeys.computeIfAbsent(key, __ -> new ArrayList<>())
                        .add(action);
            }
        }

        Map<List<ActionNode>, List<KeyStroke>> byActions = new HashMap<>();
        for (Entry<KeyStroke, List<ActionNode>> entry : byKeys.entrySet()) {
            byActions.computeIfAbsent(entry.getValue(), __ -> new ArrayList<>())
                    .add(entry.getKey());
        }

        int counter = 0;
        List<RootAction> result = new ArrayList<>();
        for (Entry<List<ActionNode>, List<KeyStroke>> entry :
                byActions.entrySet()) {

            result.add(new RootAction(
                    AppComponent.ACTION_ID_PREFIX + counter,
                    entry.getValue(),
                    entry.getKey().stream().map(it -> Pair.create(
                            it.toAction(
                                    actionManager,
                                    popupManager,
                                    popupFactory,
                                    dataManager
                            ),
                            it.when()
                    )).collect(toList())

            ));

            counter++;
        }
        return result;
    }
}
