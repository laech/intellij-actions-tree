package com.gitlab.lae.intellij.actions.tree.ui;

import com.google.auto.value.AutoValue;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Presentation;

import javax.annotation.Nullable;
import javax.swing.*;
import java.util.List;

@AutoValue
public abstract class ActionPresentation {
    ActionPresentation() {
    }

    public static ActionPresentation create(
            Presentation presentation,
            List<KeyStroke> keys,
            String separatorAbove,
            Boolean sticky,
            AnAction action
    ) {
        return new AutoValue_ActionPresentation(
                presentation,
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

    public abstract Boolean sticky();

    public abstract AnAction action();

    @Override
    public String toString() {
        String text = presentation().getText();
        if (text == null) {
            text = "";
        }
        return text;
    }

}
