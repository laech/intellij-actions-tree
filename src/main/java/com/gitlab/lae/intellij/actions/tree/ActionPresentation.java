package com.gitlab.lae.intellij.actions.tree;

import com.google.auto.value.AutoValue;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Presentation;

import javax.annotation.Nullable;
import javax.swing.*;
import java.util.List;

@AutoValue
abstract class ActionPresentation {
    ActionPresentation() {
    }

    static ActionPresentation create(
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

    abstract Presentation presentation();

    abstract List<KeyStroke> keys();

    @Nullable
    abstract String separatorAbove();

    abstract Boolean sticky();

    abstract AnAction action();

    @Override
    public String toString() {
        String text = presentation().getText();
        if (text == null) {
            text = "";
        }
        return text;
    }

}
