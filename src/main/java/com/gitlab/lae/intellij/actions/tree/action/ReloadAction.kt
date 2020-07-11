package com.gitlab.lae.intellij.actions.tree.action

import com.gitlab.lae.intellij.actions.tree.app.App
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware

class ReloadAction : AnAction(), DumbAware {
  override fun actionPerformed(e: AnActionEvent) {
    App.reload()
  }
}
