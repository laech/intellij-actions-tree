package com.gitlab.lae.intellij.actions.tree

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

internal class UnknownAction(action: ActionNode) :
  AnAction("?" + action.id + "?") {

  override fun getActionUpdateThread() = ActionUpdateThread.BGT

  override fun actionPerformed(e: AnActionEvent) {}

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = false
  }
}
