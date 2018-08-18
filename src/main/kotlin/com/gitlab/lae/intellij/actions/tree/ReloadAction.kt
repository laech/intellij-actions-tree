package com.gitlab.lae.intellij.actions.tree

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager

class ReloadAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        ApplicationManager
                .getApplication()
                .getComponent(AppComponent::class.java)
                .reload()
    }
}
