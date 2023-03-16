package com.gitlab.lae.intellij.actions.tree.ui

import com.intellij.ui.components.JBList
import java.awt.event.KeyEvent

open class ActionList(items: Collection<ActionPresentation>) : JBList<ActionPresentation>(items) {

  init {
    cellRenderer = ActionPresentationRenderer()
  }

  public override fun processKeyEvent(e: KeyEvent) {
    e.source = this
    super.processKeyEvent(e)
  }
}
