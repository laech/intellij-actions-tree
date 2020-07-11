package com.gitlab.lae.intellij.actions.tree.ui

import com.gitlab.lae.intellij.actions.tree.ActionNode.Companion.ACTION_PLACE
import com.gitlab.lae.intellij.actions.tree.util.setEnabledModalContext
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.PlatformDataKeys.IS_MODAL_CONTEXT
import com.intellij.openapi.actionSystem.ex.ActionUtil.performDumbAwareUpdate
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.JList
import javax.swing.KeyStroke

data class ActionPresentation(
  val presentation: Presentation,
  val keys: List<KeyStroke>,
  val separatorAbove: String?,
  val sticky: Boolean,
  val action: AnAction
) {

  override fun toString() = presentation.text ?: ""

  fun registerShortcuts(
    list: JList<*>,
    consumer: (ActionPresentation, ActionEvent) -> Unit
  ) {
    if (keys.isEmpty()) {
      return
    }
    val inputMap = list.inputMap
    val actionMap = list.actionMap
    for (key in keys) {
      inputMap.put(key, key)
      actionMap.put(key, object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent) {
          consumer(this@ActionPresentation, e)
        }
      })
    }
  }

  fun update(
    actionManager: ActionManager,
    dataContext: DataContext
  ) {
    val event = AnActionEvent(
      null,
      dataContext,
      ACTION_PLACE,
      presentation,
      actionManager,
      0
    )
    event.setInjectedContext(action.isInInjectedContext)
    val isModal = dataContext.getData(IS_MODAL_CONTEXT) ?: false
    performDumbAwareUpdate(isModal, action, event, false)
    setEnabledModalContext(event, action)
  }

  companion object {
    fun create(
      action: AnAction,
      keys: List<KeyStroke>,
      separatorAbove: String?,
      sticky: Boolean
    ) = ActionPresentation(
      action.templatePresentation.clone(),
      keys,
      separatorAbove,
      sticky,
      action
    )
  }
}
