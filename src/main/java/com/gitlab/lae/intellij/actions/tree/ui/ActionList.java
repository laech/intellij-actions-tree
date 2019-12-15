package com.gitlab.lae.intellij.actions.tree.ui;

import com.intellij.ui.components.JBList;

import java.awt.event.KeyEvent;
import java.util.Collection;

public class ActionList extends JBList<ActionPresentation> {

    public ActionList(Collection<ActionPresentation> items) {
        super(items);
        setCellRenderer(new ActionPresentationRenderer());
    }

    @Override
    protected void processKeyEvent(KeyEvent e) {
        e.setSource(this);
        super.processKeyEvent(e);
    }

}
