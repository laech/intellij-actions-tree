package com.gitlab.lae.intellij.actions.tree;

import org.junit.Rule;
import org.junit.Test;
import stack.source.junit4.ErrorDecorator;

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

public final class JsonTest {

    @Rule
    public final ErrorDecorator errorDecorator = new ErrorDecorator();

    @Test
    public void deserialization() throws Exception {
        List<ActionNode> expected = asList(
                ActionNode.create("ActionsTree1", "Unnamed", null, false, singletonList(getKeyStroke("ctrl C")), singletonList(
                        ActionNode.create("ActionsTree2", "Unnamed", null, false, singletonList(getKeyStroke("P")), asList(
                                ActionNode.create("CloseProject", "Unnamed", null, false,
                                        asList(getKeyStroke("K"), getKeyStroke("ctrl K")), emptyList()),
                                ActionNode.create("OpenProjectGroup", "Unnamed", "SEP", false,
                                        singletonList(getKeyStroke("P")), emptyList())
                        ))
                )),
                ActionNode.create("ActionsTree3", "b", null, false, singletonList(getKeyStroke("ctrl X")), singletonList(
                        ActionNode.create("EditorSwapSelectionBoundaries", "Unnamed", null, true,
                                singletonList(getKeyStroke("ctrl X")), emptyList())
                ))
        );

        List<ActionNode> actual;
        try (InputStream stream = JsonTest.class.getResourceAsStream("test.json");
             Reader reader = new InputStreamReader(stream, UTF_8)) {
            actual = Json.parseJsonActions(reader);
        }

        assertEquals(expected, actual);
    }

}
