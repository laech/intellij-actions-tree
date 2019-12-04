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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class RootActionTest {

    @Test
    public void disablePresentationIfNoSuitableActionFound() {
        RootAction action = new RootAction("id", emptyList(), emptyList());
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
        action.update(event);
        assertFalse(presentation.isEnabled());
    }

    @Test
    public void mergesRootActions() {
        AnAction cut = mock(AnAction.class, "cut");
        AnAction copy = mock(AnAction.class, "copy");
        AnAction paste = mock(AnAction.class, "paste");
        ActionManager actionManager = mock(ActionManager.class);
        when(actionManager.getAction(ACTION_CUT)).thenReturn(cut);
        when(actionManager.getAction(ACTION_COPY)).thenReturn(copy);
        when(actionManager.getAction(ACTION_PASTE)).thenReturn(paste);

        List<RootAction> actual = RootAction.merge(
                asList(
                        newActionNode(ACTION_CUT, When.ALWAYS, getKeyStroke('a')),
                        newActionNode(
                                ACTION_COPY,
                                When.toolWindow("Project"),
                                getKeyStroke('a'),
                                getKeyStroke('b')
                        ),
                        newActionNode(
                                ACTION_PASTE,
                                When.fileExt("txt"),
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
                                Pair.create(copy, When.toolWindow("Project")),
                                Pair.create(cut, When.ALWAYS)
                        )
                ),
                new RootAction(
                        "ActionsTree.1",
                        singletonList(getKeyStroke('b')),
                        singletonList(Pair.create(copy, When.toolWindow("Project")))
                ),
                new RootAction(
                        "ActionsTree.2",
                        asList(getKeyStroke('x'), getKeyStroke('y')),
                        singletonList(Pair.create(paste, When.fileExt("txt")))
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