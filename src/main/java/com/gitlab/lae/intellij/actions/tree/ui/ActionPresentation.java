package com.gitlab.lae.intellij.actions.tree.ui;

import com.google.auto.value.AutoValue;
import com.intellij.openapi.actionSystem.*;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.function.BiConsumer;

import static com.gitlab.lae.intellij.actions.tree.ActionNode.ACTION_PLACE;
import static com.gitlab.lae.intellij.actions.tree.util.Actions.setEnabledModalContext;
import static com.intellij.openapi.actionSystem.PlatformDataKeys.IS_MODAL_CONTEXT;
import static com.intellij.openapi.actionSystem.ex.ActionUtil.performDumbAwareUpdate;

@AutoValue
public abstract class ActionPresentation {
    ActionPresentation() {
    }

    public static ActionPresentation create(
            AnAction action,
            List<KeyStroke> keys,
            String separatorAbove,
            boolean sticky
    ) {
        return new AutoValue_ActionPresentation(
                action.getTemplatePresentation().clone(),
                keys,
                separatorAbove,
                sticky,
                action
        );
    }

    public abstract Presentation presentation();

    public abstract List<KeyStroke> keys();

    @Nullable
    abstract String separatorAbove();

    public abstract boolean sticky();

    public abstract AnAction action();

    @Override
    public String toString() {
        String text = presentation().getText();
        if (text == null) {
            text = "";
        }
        return text;
    }

    public void registerShortcuts(
            JList<?> list,
            BiConsumer<ActionPresentation, ActionEvent> consumer
    ) {
        if (keys().isEmpty()) {
            return;
        }
        InputMap inputMap = list.getInputMap();
        ActionMap actionMap = list.getActionMap();
        for (KeyStroke key : keys()) {
            inputMap.put(key, key);
            actionMap.put(key, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    consumer.accept(ActionPresentation.this, e);
                }
            });
        }
    }

    public void update(
            ActionManager actionManager,
            DataContext dataContext
    ) {
        AnActionEvent event = new AnActionEvent(
                null,
                dataContext,
                ACTION_PLACE,
                presentation(),
                actionManager,
                0
        );
        event.setInjectedContext(action().isInInjectedContext());
        Boolean isModal = dataContext.getData(IS_MODAL_CONTEXT);
        if (isModal == null) {
            isModal = false;
        }
        performDumbAwareUpdate(isModal, action(), event, false);
        setEnabledModalContext(event, action());
    }

}
