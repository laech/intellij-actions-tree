package com.gitlab.lae.intellij.actions.tree

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware

class PopupAction(
    private val action: ActionNode,
) : AnAction(action.name), DumbAware {

  init {
    isEnabledInModalContext = true
  }

  override fun actionPerformed(e: AnActionEvent) {
    Popup(action, e).show(e.dataContext)
  }
}
