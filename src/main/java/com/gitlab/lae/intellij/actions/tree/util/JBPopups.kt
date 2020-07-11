package com.gitlab.lae.intellij.actions.tree.util;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;

import javax.swing.*;

public final class JBPopups {
    private JBPopups() {
    }

    public static void setBestLocation(
            JBPopup popup,
            JBPopupFactory popupFactory,
            Editor editor
    ) {
        editor.getScrollingModel().runActionOnScrollingFinished(() ->
                popup.setLocation(popupFactory
                        .guessBestPopupLocation(editor)
                        .getScreenPoint()));
    }

    public static void setBestLocation(
            JBPopup popup,
            JBPopupFactory popupFactory,
            JComponent component
    ) {
        popup.setLocation(popupFactory
                .guessBestPopupLocation(component)
                .getScreenPoint());
    }

}
