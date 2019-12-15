package com.gitlab.lae.intellij.actions.tree.app;

import com.gitlab.lae.intellij.actions.tree.ActionNode;
import com.gitlab.lae.intellij.actions.tree.When;
import com.intellij.ide.DataManager;
import com.intellij.ide.IdePopupManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.wm.IdeFocusManager;
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
    public void disablesPresentationIfNoSuitableActionFound() {
        var enable = new EnableAction();
        var when = mock(When.class);
        var action = new RootAction(
                "id",
                emptyList(),
                singletonList(Pair.create(enable, when))
        );
        var presentation = new Presentation();
        presentation.setEnabled(true);
        var event = new AnActionEvent(
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
    public void mergingMaintainsCustomActionGroupsIds() {
        var id = "my-custom-group-id";
        var actual = RootAction.merge(
                singletonList(newActionNode(
                        id,
                        When.ALWAYS,
                        singletonList(getKeyStroke("X")),
                        singletonList(newActionNode(
                                "bob",
                                When.ALWAYS,
                                emptyList(),
                                emptyList()
                        ))
                )),
                mock(ActionManager.class),
                mock(IdeFocusManager.class),
                new IdePopupManager(),
                mock(JBPopupFactory.class),
                mock(DataManager.class)
        );
        assertEquals(1, actual.size());
        assertEquals(id, actual.get(0).getId());
    }

    @Test
    public void mergesRootActions() {
        AnAction cut = new EmptyAction("cut", null, null);
        AnAction copy = new EmptyAction("copy", null, null);
        AnAction paste = new EmptyAction("paste", null, null);
        var actionManager = mock(ActionManager.class);
        when(actionManager.getAction(ACTION_CUT)).thenReturn(cut);
        when(actionManager.getAction(ACTION_COPY)).thenReturn(copy);
        when(actionManager.getAction(ACTION_PASTE)).thenReturn(paste);

        var actual = RootAction.merge(
                asList(
                        newActionNode(
                                ACTION_CUT,
                                When.ALWAYS,
                                singletonList(getKeyStroke('a')),
                                emptyList()
                        ),
                        newActionNode(
                                ACTION_COPY,
                                When.toolWindowActive("Project"),
                                asList(getKeyStroke('a'), getKeyStroke('b')),
                                emptyList()
                        ),
                        newActionNode(
                                ACTION_PASTE,
                                When.fileExtension("txt"),
                                asList(getKeyStroke('x'), getKeyStroke('y')),
                                emptyList()
                        )
                ),
                actionManager,
                null,
                null,
                null,
                null
        );

        var expected = asList(
                new RootAction(
                        "ActionsTree.Root.0",
                        singletonList(getKeyStroke('a')),
                        asList(
                                Pair.create(
                                        copy,
                                        When.toolWindowActive("Project")
                                ),
                                Pair.create(cut, When.ALWAYS)
                        )
                ),
                new RootAction(
                        "ActionsTree.Root.1",
                        singletonList(getKeyStroke('b')),
                        singletonList(Pair.create(
                                copy,
                                When.toolWindowActive("Project")
                        ))
                ),
                new RootAction(
                        "ActionsTree.Root.2",
                        asList(getKeyStroke('x'), getKeyStroke('y')),
                        singletonList(Pair.create(
                                paste,
                                When.fileExtension("txt")
                        ))
                )
        );

        assertEquals(expected, actual);
    }

    private static ActionNode newActionNode(
            String id,
            When when,
            List<KeyStroke> keyStrokes,
            List<ActionNode> items
    ) {
        return ActionNode.create(
                id,
                null,
                null,
                false,
                when,
                keyStrokes,
                items
        );
    }

    private static final class ModalAction extends AnAction {
        ModalAction(boolean enableInModalContext) {
            setEnabledInModalContext(enableInModalContext);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
        }
    }

    @Test
    public void enableInModalIfAnyActionSupportsModal() {
        assertTrue(new RootAction("id", emptyList(), asList(
                Pair.create(new ModalAction(true), When.NEVER),
                Pair.create(new ModalAction(false), When.NEVER)
        )).isEnabledInModalContext());

        assertFalse(new RootAction("id", emptyList(), asList(
                Pair.create(new ModalAction(false), When.NEVER),
                Pair.create(new ModalAction(false), When.NEVER)
        )).isEnabledInModalContext());
    }
}