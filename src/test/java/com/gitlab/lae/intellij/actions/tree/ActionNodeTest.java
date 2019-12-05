package com.gitlab.lae.intellij.actions.tree;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.util.Pair;
import org.junit.Test;

import javax.swing.*;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public final class ActionNodeTest {

    @Test
    public void preparesChildItemForContext() {
        ActionNode a = ActionNode.create(
                "a",
                "a",
                null,
                false,
                When.ALWAYS,
                singletonList(getKeyStroke('a')),
                emptyList()
        );
        ActionNode b = ActionNode.create(
                "b",
                "b",
                null,
                false,
                When.ALWAYS,
                asList(getKeyStroke('b'), getKeyStroke('z')),
                emptyList()
        );
        ActionNode c = ActionNode.create(
                "c",
                "c",
                null,
                false,
                When.ALWAYS,
                singletonList(getKeyStroke('b')),
                emptyList()
        );
        ActionNode d = ActionNode.create(
                "d",
                "d",
                null,
                false,
                When.NEVER,
                singletonList(getKeyStroke('b')),
                emptyList()
        );

        List<Pair<List<KeyStroke>, ActionNode>> actual = ActionNode.create(
                "id",
                "name",
                null,
                false,
                When.ALWAYS,
                emptyList(),
                asList(a, b, c, d)
        ).prepare(mock(DataContext.class));

        List<Pair<List<KeyStroke>, ActionNode>> expected = asList(
                Pair.create(singletonList(getKeyStroke('a')), a),
                Pair.create(singletonList(getKeyStroke('z')), b),
                Pair.create(singletonList(getKeyStroke('b')), c)
        );

        assertEquals(expected, actual);
    }
}