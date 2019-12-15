package com.gitlab.lae.intellij.actions.tree.util;

import com.intellij.openapi.actionSystem.*;
import org.junit.Test;

import static com.intellij.openapi.actionSystem.PlatformDataKeys.IS_MODAL_CONTEXT;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ActionsTest {

    private static final class ModalAction extends AnAction {
        ModalAction(boolean enableInModalContext) {
            setEnabledInModalContext(enableInModalContext);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
        }
    }

    @Test
    public void disablesPresentationInModalContextIfActionDoesNotSupportIt() {
        DataContext context = mock(DataContext.class);
        AnActionEvent event = new AnActionEvent(
                null,
                context,
                "",
                new Presentation(),
                mock(ActionManager.class),
                0
        );
        AnAction action = new ModalAction(false);

        when(context.getData(IS_MODAL_CONTEXT)).thenReturn(true);
        Actions.setEnabledModalContext(event, action);
        assertFalse(event.getPresentation().isEnabled());

        when(context.getData(IS_MODAL_CONTEXT)).thenReturn(false);
        Actions.setEnabledModalContext(event, action);
        assertFalse(event.getPresentation().isEnabled());
    }

    @Test
    public void doesNotEnablePresentationInModalContextIfPresentationIsAlreadyDisabled() {
        DataContext context = mock(DataContext.class);
        AnActionEvent event = new AnActionEvent(
                null,
                context,
                "",
                new Presentation(),
                mock(ActionManager.class),
                0
        );
        AnAction action = new ModalAction(true);
        event.getPresentation().setEnabled(false);
        when(context.getData(IS_MODAL_CONTEXT)).thenReturn(true);
        Actions.setEnabledModalContext(event, action);
        assertFalse(event.getPresentation().isEnabled());
    }
}