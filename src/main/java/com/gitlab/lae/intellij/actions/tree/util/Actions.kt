package com.gitlab.lae.intellij.actions.tree.util;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;

import static com.intellij.openapi.actionSystem.PlatformDataKeys.IS_MODAL_CONTEXT;

public final class Actions {
    private Actions() {
    }

    public static void setEnabledModalContext(
            AnActionEvent event,
            AnAction action
    ) {
        Presentation presentation = event.getPresentation();
        if (presentation.isEnabled()
                && isModalContext(event)
                && !action.isEnabledInModalContext()) {
            presentation.setEnabled(false);
        }
    }

    private static boolean isModalContext(AnActionEvent e) {
        Boolean modal = e.getData(IS_MODAL_CONTEXT);
        if (modal == null) {
            modal = false;
        }
        return modal;
    }

}
