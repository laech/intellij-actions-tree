package com.gitlab.lae.intellij.actions.tree;

import com.intellij.openapi.ui.popup.IdePopupEventDispatcher;
import com.intellij.openapi.ui.popup.JBPopup;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Using an {@link IdePopupEventDispatcher} is required to let IntelliJ
 * know we have a popup showing, therefore keyboard events should
 * be forward to us for processing first.
 * <p>
 * For example, without this if the popup has an item in the list
 * with shortcut "Ctrl+D", and there is a global shortcut specified
 * in the IntelliJ keymap for "Ctrl+D Ctrl+X" (2 key strokes),
 * then when "Ctrl+D" is pressed while the popup is showing,
 * the entry in the popup won't be executed, instead IntelliJ will
 * go into waiting for the second key stroke, since it sees "Ctrl+D"
 * as a prefix key. We want the entry in the popup to be execute
 * when "Ctrl+D" is pressed.
 */
final class ActionDispatcher implements IdePopupEventDispatcher {

    private final JBPopup popup;
    private final ActionList<?> list;

    ActionDispatcher(JBPopup popup, ActionList<?> list) {
        this.popup = requireNonNull(popup);
        this.list = requireNonNull(list);
    }

    @Override
    public Stream<JBPopup> getPopupStream() {
        return Stream.of(popup);
    }

    @Override
    public boolean dispatch(AWTEvent event) {
        if (!(event instanceof KeyEvent)) {
            return false;
        }
        KeyEvent keyEvent = (KeyEvent) event;
        list.processKeyEvent(keyEvent);
        return keyEvent.isConsumed();
    }

    @Override
    public Component getComponent() {
        return popup.getContent();
    }

    @Override
    public boolean requestFocus() {
        popup.getContent().requestFocus();
        return true;
    }

    @Override
    public boolean close() {
        popup.closeOk(null);
        return true;
    }

    @Override
    public void setRestoreFocusSilentely() {
    }
}
