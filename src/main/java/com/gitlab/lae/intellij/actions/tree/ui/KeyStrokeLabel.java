package com.gitlab.lae.intellij.actions.tree.ui;

import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;

import static java.awt.event.KeyEvent.*;

/**
 * Make keystroke labels look vertically aligned better
 * when proportional font is used.
 * <p>
 * For example, when "^F" and "^B" appears above/below each
 * other in the popup, because "F" and "B" and different fonts widths,
 * they look unaligned and out of place. This looks worst when multiple
 * keystrokes are assigned to and item.
 */
final class KeyStrokeLabel {

    private final JPanel panel =
            new JPanel(new FlowLayout(FlowLayout.TRAILING, 0, 0));

    private final JLabel first =
            new JLabel((String) null, SwingConstants.TRAILING);

    private final JLabel second =
            new JLabel((String) null, SwingConstants.CENTER);

    {
        first.setBackground(null);
        second.setBackground(null);
        panel.add(first);
        panel.add(second);
        panel.setBackground(null);
        panel.setBorder(JBUI.Borders.emptyLeft(5));
    }

    Component getComponent() {
        return panel;
    }

    void setForeground(Color fg) {
        first.setForeground(fg);
        second.setForeground(fg);
    }

    void setEnabled(boolean enabled) {
        first.setEnabled(enabled);
        second.setEnabled(enabled);
    }

    void setTextFromKeyStroke(KeyStroke key) {

        first.setPreferredSize(null);
        second.setPreferredSize(null);
        panel.removeAll();

        if (key == null) return;

        Entry<String, String> entry = getKeyTextParts(key);
        String prefix = entry.getKey();
        String suffix = entry.getValue();
        first.setText(prefix);
        panel.add(first);

        second.setText(suffix);
        second.setPreferredSize(suffix.length() > 1
                ? null : getSecondCharSize());

        panel.add(second);
    }

    private Dimension getSecondCharSize() {
        FontMetrics ms = second.getFontMetrics(second.getFont());
        int width = Integer.max(ms.stringWidth("W"), ms.stringWidth("@"));
        return new Dimension(width, ms.getHeight());
    }

    static String getKeyText(KeyStroke key) {
        Entry<String, String> entry = getKeyTextParts(key);
        return entry.getKey() + entry.getValue();
    }

    private static Entry<String, String> getKeyTextParts(KeyStroke key) {
        KeyStroke copy = KeyStroke.getKeyStroke(
                VK_A,
                key.getModifiers(),
                key.isOnKeyRelease()
        );
        String prefix = KeymapUtil.getKeystrokeText(copy);
        prefix = prefix.substring(0, prefix.length() - 1);
        String suffix = key.getKeyChar() != CHAR_UNDEFINED
                ? String.valueOf(key.getKeyChar())
                : getKeyText(key.getKeyCode());
        return new SimpleImmutableEntry<>(prefix, suffix);
    }

    private static String getKeyText(int keyCode) {
        if (keyCode == VK_MINUS) return "-";
        if (keyCode == VK_COMMA) return ",";
        if (keyCode == VK_QUOTE) return "'";
        return KeymapUtil.getKeyText(keyCode);
    }
}
