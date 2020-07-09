package com.gitlab.lae.intellij.actions.tree;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.util.Pair;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public final class ActionNodeTest {

    @Test
    public void preparesChildItemForContext() {
        var a = ActionNode.create(
                "a",
                "a",
                null,
                false,
                When.ALWAYS,
                singletonList(getKeyStroke('a')),
                emptyList()
        );
        var b = ActionNode.create(
                "b",
                "b",
                null,
                false,
                When.ALWAYS,
                asList(getKeyStroke('b'), getKeyStroke('z')),
                emptyList()
        );
        var c = ActionNode.create(
                "c",
                "c",
                null,
                false,
                When.ALWAYS,
                singletonList(getKeyStroke('b')),
                emptyList()
        );
        var d = ActionNode.create(
                "d",
                "d",
                null,
                false,
                When.NEVER,
                singletonList(getKeyStroke('b')),
                emptyList()
        );

        var actual = ActionNode.create(
                "id",
                "name",
                null,
                false,
                When.ALWAYS,
                emptyList(),
                asList(a, b, c, d)
        ).prepare(mock(DataContext.class));

        var expected = asList(
                Pair.create(singletonList(getKeyStroke('a')), a),
                Pair.create(singletonList(getKeyStroke('z')), b),
                Pair.create(singletonList(getKeyStroke('b')), c)
        );

        assertEquals(expected, actual);
    }

    @Test
    public void preparesChildItemForContextKeepsEmptyKeyStrokes() {
        var a = ActionNode.create(
                "a",
                "a",
                null,
                false,
                When.ALWAYS,
                emptyList(),
                emptyList()
        );
        var b = ActionNode.create(
                "b",
                "b",
                null,
                false,
                When.ALWAYS,
                singletonList(getKeyStroke('b')),
                emptyList()
        );
        var c = ActionNode.create(
                "c",
                "c",
                null,
                false,
                When.ALWAYS,
                emptyList(),
                emptyList()
        );

        var actual = ActionNode.create(
                "id",
                "name",
                null,
                false,
                When.ALWAYS,
                emptyList(),
                asList(a, b, c)
        ).prepare(mock(DataContext.class));

        var expected = asList(
                Pair.create(emptyList(), a),
                Pair.create(singletonList(getKeyStroke('b')), b),
                Pair.create(emptyList(), c)
        );

        assertEquals(expected, actual);
    }
}