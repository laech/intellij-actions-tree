package com.gitlab.lae.intellij.actions.tree;

import com.intellij.ide.DataManager;
import com.intellij.ide.IdePopupManager;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class PopupActionTest {

    @Test
    public void enabledInModalContext() {
        assertTrue(newPopupAction().isEnabledInModalContext());
    }

    @Test
    public void isDumpAware() {
        assertTrue(newPopupAction().isDumbAware());
    }

    private PopupAction newPopupAction() {
        return new PopupAction(
                mock(ActionNode.class),
                new IdePopupManager(),
                mock(JBPopupFactory.class),
                mock(DataManager.class)
        );
    }
}