package com.gitlab.lae.intellij.actions.tree

import com.intellij.ui.components.JBList
import java.awt.event.KeyEvent

class ActionList<E>(items: Collection<E>) : JBList<E>(items) {

    public override fun processKeyEvent(e: KeyEvent) {
        e.source = this
        super.processKeyEvent(e)
    }
}
