package com.gitlab.lae.intellij.actions.tree

import com.intellij.ide.DataManager
import com.intellij.ide.IdePopupManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.IdeFocusManager

class PopupAction(
  private val action: ActionNode,
  private val focusManager: IdeFocusManager,
  private val popupManager: IdePopupManager,
  private val popupFactory: JBPopupFactory,
  private val dataManager: DataManager,
) : AnAction(action.name), DumbAware {

  init {
    isEnabledInModalContext = true
  }

  override fun actionPerformed(e: AnActionEvent) {
    Popup(
      action,
      e,
      focusManager,
      popupManager,
      popupFactory,
      dataManager,
    ).show(e.dataContext)
  }
}
