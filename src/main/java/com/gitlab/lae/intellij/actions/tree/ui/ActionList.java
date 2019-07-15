package com.gitlab.lae.intellij.actions.tree.ui;

import com.intellij.ui.components.JBList;

import java.awt.event.KeyEvent;
import java.util.Collection;

public final class ActionList<E> extends JBList<E> {

    public ActionList(Collection<E> items) {
        super(items);
    }

    @Override
    protected void processKeyEvent(KeyEvent e) {
        e.setSource(this);
        super.processKeyEvent(e);
    }

}
