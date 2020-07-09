package com.gitlab.lae.intellij.actions.tree.action

import com.gitlab.lae.intellij.actions.tree.app.AppComponent
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware

class ReloadAction : AnAction(), DumbAware {
  override fun actionPerformed(e: AnActionEvent) {
    ApplicationManager
      .getApplication()
      .getComponent(AppComponent::class.java)
      .reload()
  }
}
