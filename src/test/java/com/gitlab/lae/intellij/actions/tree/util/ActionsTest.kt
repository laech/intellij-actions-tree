package com.gitlab.lae.intellij.actions.tree.util

import com.gitlab.lae.intellij.actions.tree.actionEvent
import com.gitlab.lae.intellij.actions.tree.util.Actions.setEnabledModalContext
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys.IS_MODAL_CONTEXT
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertFalse
import org.junit.Test

class ActionsTest {

  private class ModalAction(enableInModalContext: Boolean) : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {}

    init {
      isEnabledInModalContext = enableInModalContext
    }
  }

  @Test
  fun `disables presentation in modal context if action does not support it`() {
    val context = mock<DataContext>()
    val event = actionEvent(context = context)
    val action = ModalAction(false)

    whenever(context.getData(IS_MODAL_CONTEXT)).thenReturn(true)
    setEnabledModalContext(event, action)
    assertFalse(event.presentation.isEnabled)

    whenever(context.getData(IS_MODAL_CONTEXT)).thenReturn(false)
    setEnabledModalContext(event, action)
    assertFalse(event.presentation.isEnabled)
  }

  @Test
  fun `does not enable presentation in modal context if presentation is already disabled`() {
    val context = mock<DataContext>()
    val event = actionEvent(context = context)
    val action = ModalAction(true)
    event.presentation.isEnabled = false
    whenever(context.getData(IS_MODAL_CONTEXT)).thenReturn(true)
    setEnabledModalContext(event, action)
    assertFalse(event.presentation.isEnabled)
  }
}
