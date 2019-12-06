package com.gitlab.lae.intellij.actions.tree.app;

import com.gitlab.lae.intellij.actions.tree.ActionNode;
import com.gitlab.lae.intellij.actions.tree.When;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.util.Pair;
import org.junit.Test;

import javax.swing.*;
import java.util.List;

import static com.intellij.openapi.actionSystem.IdeActions.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class RootActionTest {

    private static final class EnableAction extends AnAction {
        boolean enabled;

        @Override
        public void update(AnActionEvent e) {
            super.update(e);
            e.getPresentation().setEnabled(enabled);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
        }
    }

    @Test
    public void disableOPresentationIfNoSuitableActionFound() {
        EnableAction enable = new EnableAction();
        When when = mock(When.class);
        RootAction action = new RootAction(
                "id",
                emptyList(),
                singletonList(Pair.create(enable, when))
        );
        Presentation presentation = new Presentation();
        presentation.setEnabled(true);
        AnActionEvent event = new AnActionEvent(
                null,
                mock(DataContext.class),
                "",
                presentation,
                mock(ActionManager.class),
                0
        );

        assertTrue(presentation.isEnabled());

        enable.enabled = true;
        when(when.test(any())).thenReturn(false);
        action.update(event);
        assertFalse(presentation.isEnabled());

        when(when.test(any())).thenReturn(true);
        action.update(event);
        assertTrue(presentation.isEnabled());

        enable.enabled = false;
        action.update(event);
        assertFalse(presentation.isEnabled());
    }

    @Test
    public void mergesRootActions() {
        AnAction cut = new EmptyAction("cut", null, null);
        AnAction copy = new EmptyAction("copy", null, null);
        AnAction paste = new EmptyAction("paste", null, null);
        ActionManager actionManager = mock(ActionManager.class);
        when(actionManager.getAction(ACTION_CUT)).thenReturn(cut);
        when(actionManager.getAction(ACTION_COPY)).thenReturn(copy);
        when(actionManager.getAction(ACTION_PASTE)).thenReturn(paste);

        List<RootAction> actual = RootAction.merge(
                asList(
                        newActionNode(
                                ACTION_CUT,
                                When.ALWAYS,
                                getKeyStroke('a')
                        ),
                        newActionNode(
                                ACTION_COPY,
                                When.toolWindowActive("Project"),
                                getKeyStroke('a'),
                                getKeyStroke('b')
                        ),
                        newActionNode(
                                ACTION_PASTE,
                                When.fileExtension("txt"),
                                getKeyStroke('x'),
                                getKeyStroke('y')
                        )
                ),
                actionManager,
                null,
                null,
                null
        );

        List<RootAction> expected = asList(
                new RootAction(
                        "ActionsTree.0",
                        singletonList(getKeyStroke('a')),
                        asList(
                                Pair.create(copy, When.toolWindowActive("Project")),
                                Pair.create(cut, When.ALWAYS)
                        )
                ),
                new RootAction(
                        "ActionsTree.1",
                        singletonList(getKeyStroke('b')),
                        singletonList(Pair.create(
                                copy,
                                When.toolWindowActive("Project")
                        ))
                ),
                new RootAction(
                        "ActionsTree.2",
                        asList(getKeyStroke('x'), getKeyStroke('y')),
                        singletonList(Pair.create(paste, When.fileExtension("txt")))
                )
        );

        assertEquals(expected, actual);
    }

    private static ActionNode newActionNode(
            String id,
            When when,
            KeyStroke... keyStrokes
    ) {
        return ActionNode.create(
                id,
                null,
                null,
                false,
                when,
                asList(keyStrokes),
                emptyList()
        );
    }
}