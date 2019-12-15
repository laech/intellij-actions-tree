package com.gitlab.lae.intellij.actions.tree.ui;

import com.intellij.ide.IdePopupManager;
import com.intellij.openapi.ui.popup.JBPopup;
import org.junit.Test;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.List;

import static java.awt.event.KeyEvent.CHAR_UNDEFINED;
import static java.awt.event.KeyEvent.VK_A;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class ActionPopupEventDispatcherTest {

    @Test
    public void forwardsKeyEvent() {
        ActionList list = mock(ActionList.class);
        ActionPopupEventDispatcher dispatcher =
                new ActionPopupEventDispatcher(
                        mock(JBPopup.class),
                        list,
                        new IdePopupManager()
                );

        KeyEvent event = new KeyEvent(
                new JLabel(), 0, 0, 0, VK_A, CHAR_UNDEFINED
        );
        dispatcher.dispatch(event);
        verify(list).processKeyEvent(event);
    }

    @Test
    public void informsIdePopupManagerOfPopupState() throws Exception {
        IdePopupManager idePopupManager = new IdePopupManager();
        ActionPopupEventDispatcher dispatcher =
                new ActionPopupEventDispatcher(
                        mock(JBPopup.class),
                        mock(ActionList.class),
                        idePopupManager
                );

        List<?> myDispatchStack = getDispatchStack(idePopupManager);

        assertFalse(myDispatchStack.contains(dispatcher));
        dispatcher.beforeShown(null);
        assertTrue(myDispatchStack.contains(dispatcher));
        dispatcher.onClosed(null);
        assertFalse(myDispatchStack.contains(dispatcher));
    }

    private List<?> getDispatchStack(IdePopupManager manager)
            throws ReflectiveOperationException {
        Field myDispatchStackField =
                IdePopupManager.class.getDeclaredField("myDispatchStack");
        myDispatchStackField.setAccessible(true);
        return (List<?>) myDispatchStackField.get(manager);
    }
}