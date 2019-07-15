package com.gitlab.lae.intellij.actions.tree.util;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;

import javax.swing.*;

public final class JBPopups {
    private JBPopups() {
    }

    public static void setBestLocation(JBPopup popup, Editor editor) {
        editor.getScrollingModel().runActionOnScrollingFinished(() ->
                popup.setLocation(JBPopupFactory.getInstance()
                        .guessBestPopupLocation(editor)
                        .getScreenPoint()));
    }

    public static void setBestLocation(JBPopup popup, JComponent component) {
        popup.setLocation(JBPopupFactory.getInstance()
                .guessBestPopupLocation(component)
                .getScreenPoint());
    }

}
