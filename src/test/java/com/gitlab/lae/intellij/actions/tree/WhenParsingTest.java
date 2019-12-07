package com.gitlab.lae.intellij.actions.tree;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public final class WhenParsingTest {

    @Parameters(name = "{0}")
    public static Object[][] parameters() {
        return new Object[][]{
                {"ToolWindowActive:Project", When.toolWindowActive("Project")},
                {"ToolWindowTabActive:Log", When.toolWindowTabActive("Log")},
                {"FileExtension:rs", When.fileExtension("rs")},
                {"!FileExtension:rs", When.not(When.fileExtension("rs"))},
        };
    }

    private final String input;
    private final When expected;

    public WhenParsingTest(String input, When expected) {
        this.input = requireNonNull(input);
        this.expected = requireNonNull(expected);
    }

    @Test
    public void parsesInputCorrectly() {
        assertEquals(expected, When.parse(input));
    }
}
