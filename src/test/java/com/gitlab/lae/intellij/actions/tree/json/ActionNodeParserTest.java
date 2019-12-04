package com.gitlab.lae.intellij.actions.tree.json;

import com.gitlab.lae.intellij.actions.tree.ActionNode;
import com.gitlab.lae.intellij.actions.tree.When;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.junit.Assert.assertEquals;

public final class ActionNodeParserTest {

    @Test
    public void deserialization() throws Exception {
        List<ActionNode> expected = asList(
                ActionNode.create(
                        "ActionsTree1",
                        "Unnamed",
                        null,
                        false,
                        When.toolWindow("Project"),
                        singletonList(getKeyStroke("ctrl C")),
                        singletonList(
                                ActionNode.create(
                                        "ActionsTree2",
                                        "Unnamed",
                                        null,
                                        false,
                                        When.ALWAYS,
                                        singletonList(getKeyStroke("P")),
                                        asList(
                                                ActionNode.create(
                                                        "CloseProject",
                                                        "Unnamed",
                                                        null,
                                                        false,
                                                        When.ALWAYS,
                                                        asList(
                                                                getKeyStroke(
                                                                        "K"),
                                                                getKeyStroke(
                                                                        "ctrl K")
                                                        ),
                                                        emptyList()
                                                ),
                                                ActionNode.create(
                                                        "OpenProjectGroup",
                                                        "Unnamed",
                                                        "SEP",
                                                        false,
                                                        When.ALWAYS,
                                                        singletonList(
                                                                getKeyStroke(
                                                                        "P")),
                                                        emptyList()
                                                )
                                        )
                                )
                        )
                ),
                ActionNode.create(
                        "ActionsTree3",
                        "b",
                        null,
                        false,
                        When.fileExt("java"),
                        singletonList(getKeyStroke("ctrl X")),
                        singletonList(
                                ActionNode.create(
                                        "EditorSwapSelectionBoundaries",
                                        "Unnamed",
                                        null,
                                        true,
                                        When.ALWAYS,
                                        singletonList(getKeyStroke("ctrl X")),
                                        emptyList()
                                )
                        )
                ),
                ActionNode.create(
                        "ActionsTree4",
                        "c",
                        null,
                        false,
                        When.any(
                                When.fileExt("txt"),
                                When.all(
                                        When.toolWindow("Run"),
                                        When.fileExt("java")
                                )
                        ),
                        emptyList(),
                        emptyList()
                )
        );

        List<ActionNode> actual;
        try (InputStream stream = ActionNodeParserTest.class
                .getResourceAsStream("test.json");
             Reader reader = new InputStreamReader(stream, UTF_8)) {
            actual = ActionNodeParser.parseJsonActions(reader);
        }

        assertEquals(expected, actual);
    }

}
