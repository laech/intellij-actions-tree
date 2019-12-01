package com.gitlab.lae.intellij.actions.tree;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import static java.util.Objects.requireNonNull;

final class RefAction extends AnAction {

    private final AnAction action;

    RefAction(AnAction action) {
        this.action = requireNonNull(action);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        action.actionPerformed(e);
    }

    @Override
    public void beforeActionPerformedUpdate(AnActionEvent e) {
        super.beforeActionPerformedUpdate(e);
        action.beforeActionPerformedUpdate(e);
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        action.update(e);
    }

    @Override
    public boolean displayTextInToolbar() {
        return action.displayTextInToolbar();
    }

    @Override
    public void setDefaultIcon(boolean isDefaultIconSet) {
        super.setDefaultIcon(isDefaultIconSet);
        action.setDefaultIcon(isDefaultIconSet);
    }

    @Override
    public boolean isDefaultIcon() {
        return action.isDefaultIcon();
    }

    @Override
    public void setInjectedContext(boolean worksInInjected) {
        super.setInjectedContext(worksInInjected);
        action.setInjectedContext(worksInInjected);
    }

    @Override
    public boolean isInInjectedContext() {
        return action.isInInjectedContext();
    }

    @Override
    public boolean isTransparentUpdate() {
        return action.isTransparentUpdate();
    }

    @Override
    public boolean isDumbAware() {
        return action.isDumbAware();
    }

    @Override
    public boolean startInTransaction() {
        return action.startInTransaction();
    }
}
