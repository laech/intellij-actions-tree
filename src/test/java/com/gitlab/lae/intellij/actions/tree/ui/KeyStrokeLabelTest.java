package com.gitlab.lae.intellij.actions.tree.ui;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import stack.source.junit4.ErrorDecorator;

import javax.swing.*;

import static java.awt.event.KeyEvent.*;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public final class KeyStrokeLabelTest {

    @Rule
    public final ErrorDecorator errorDecorator = new ErrorDecorator();

    private final String expected;
    private final KeyStroke keyStroke;

    public KeyStrokeLabelTest(String expected, KeyStroke keyStroke) {
        this.expected = expected;
        this.keyStroke = keyStroke;
    }

    @Parameters(name = "{0}, {1}")
    public static Object[] data() {
        return new Object[]{
                new Object[]{"`", getKeyStroke(VK_BACK_QUOTE, 0)},
                new Object[]{"\\", getKeyStroke(VK_BACK_SLASH, 0)},
                new Object[]{"[", getKeyStroke(VK_OPEN_BRACKET, 0)},
                new Object[]{"]", getKeyStroke(VK_CLOSE_BRACKET, 0)},
                new Object[]{",", getKeyStroke(VK_COMMA, 0)},
                new Object[]{"=", getKeyStroke(VK_EQUALS, 0)},
                new Object[]{"-", getKeyStroke(VK_MINUS, 0)},
                new Object[]{".", getKeyStroke(VK_PERIOD, 0)},
                new Object[]{"'", getKeyStroke(VK_QUOTE, 0)},
                new Object[]{";", getKeyStroke(VK_SEMICOLON, 0)},
                new Object[]{"/", getKeyStroke(VK_SLASH, 0)},
                new Object[]{"@", getKeyStroke('@')},
                new Object[]{"<", getKeyStroke('<')},
                new Object[]{"|", getKeyStroke('|')},
        };
    }

    @Test
    public void test() {
        assertEquals(expected, KeyStrokeLabel.getKeyText(keyStroke));
    }
}
