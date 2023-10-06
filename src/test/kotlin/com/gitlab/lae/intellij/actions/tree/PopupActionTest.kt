package com.gitlab.lae.intellij.actions.tree

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PopupActionTest {

  @Test
  fun `enabled in modal context`() {
    assertTrue(newPopupAction().isEnabledInModalContext)
  }

  @Test
  fun `is dump aware`() {
    assertTrue(newPopupAction().isDumbAware)
  }

  private fun newPopupAction() = PopupAction(actionNode())
}
