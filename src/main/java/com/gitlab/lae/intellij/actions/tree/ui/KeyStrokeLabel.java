package com.gitlab.lae.intellij.actions.tree.ui;

import com.intellij.openapi.keymap.KeymapUtil;

import javax.swing.*;

import static java.awt.event.KeyEvent.*;

final class KeyStrokeLabel {

    static String getKeyText(KeyStroke key) {
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
        return prefix + suffix;
    }

    private static String getKeyText(int keyCode) {
        switch (keyCode) {
            case VK_MINUS: return "-";
            case VK_COMMA: return ",";
            case VK_QUOTE: return "'";
            default: return KeymapUtil.getKeyText(keyCode);
        }
    }
}
