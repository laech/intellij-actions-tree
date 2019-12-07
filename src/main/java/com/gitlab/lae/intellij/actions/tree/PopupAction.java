package com.gitlab.lae.intellij.actions.tree;

import com.intellij.ide.DataManager;
import com.intellij.ide.IdePopupManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.ui.popup.JBPopupFactory;

import static java.util.Objects.requireNonNull;

final class PopupAction extends AnAction implements DumbAware {

    private final ActionNode action;
    private final IdePopupManager popupManager;
    private final JBPopupFactory popupFactory;
    private final DataManager dataManager;

    PopupAction(
            ActionNode action,
            IdePopupManager popupManager,
            JBPopupFactory popupFactory,
            DataManager dataManager
    ) {
        super(action.name());
        this.action = requireNonNull(action);
        this.popupManager = requireNonNull(popupManager);
        this.popupFactory = requireNonNull(popupFactory);
        this.dataManager = requireNonNull(dataManager);
        setEnabledInModalContext(true);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        new Popup(
                action,
                e,
                popupManager,
                popupFactory,
                dataManager
        ).show(e.getDataContext());
    }

}
