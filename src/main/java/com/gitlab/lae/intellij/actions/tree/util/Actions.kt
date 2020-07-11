package com.gitlab.lae.intellij.actions.tree.util

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys.IS_MODAL_CONTEXT


fun setEnabledModalContext(event: AnActionEvent, action: AnAction) {
  if (event.presentation.isEnabled
    && isModalContext(event)
    && !action.isEnabledInModalContext
  ) {
    event.presentation.isEnabled = false
  }
}

private fun isModalContext(e: AnActionEvent): Boolean =
  e.getData(IS_MODAL_CONTEXT) ?: false
