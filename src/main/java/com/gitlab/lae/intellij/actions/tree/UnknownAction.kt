package com.gitlab.lae.intellij.actions.tree;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

final class UnknownAction extends AnAction {

    UnknownAction(ActionNode action) {
        super("?" + action.id() + "?");
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabled(false);
    }
}
