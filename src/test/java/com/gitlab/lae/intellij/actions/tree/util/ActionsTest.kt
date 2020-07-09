package com.gitlab.lae.intellij.actions.tree.util

import com.gitlab.lae.intellij.actions.tree.util.Actions.setEnabledModalContext
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.PlatformDataKeys.IS_MODAL_CONTEXT
import org.junit.Assert.assertFalse
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class ActionsTest {

  private class ModalAction(enableInModalContext: Boolean) : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {}

    init {
      isEnabledInModalContext = enableInModalContext
    }
  }

  @Test
  fun `disables presentation in modal context if action does not support it`() {
    val context = mock(DataContext::class.java)
    val event = AnActionEvent(
      null,
      context,
      "",
      Presentation(),
      mock(ActionManager::class.java),
      0
    )

    val action = ModalAction(false)
    `when`(context.getData(IS_MODAL_CONTEXT)).thenReturn(true)
    setEnabledModalContext(event, action)
    assertFalse(event.presentation.isEnabled)

    `when`(context.getData(IS_MODAL_CONTEXT)).thenReturn(false)
    setEnabledModalContext(event, action)
    assertFalse(event.presentation.isEnabled)
  }

  @Test
  fun `does not enable presentation in modal context if presentation is already disabled`() {
    val context = mock(DataContext::class.java)
    val event = AnActionEvent(
      null,
      context,
      "",
      Presentation(),
      mock(ActionManager::class.java),
      0
    )

    val action = ModalAction(true)
    event.presentation.isEnabled = false
    `when`(context.getData(IS_MODAL_CONTEXT)).thenReturn(true)
    setEnabledModalContext(event, action)
    assertFalse(event.presentation.isEnabled)
  }
}
