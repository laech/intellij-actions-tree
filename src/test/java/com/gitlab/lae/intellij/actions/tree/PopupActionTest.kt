package com.gitlab.lae.intellij.actions.tree

import com.intellij.ide.IdePopupManager
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertTrue
import org.junit.Test

class PopupActionTest {

  @Test
  fun `enabled in modal context`() {
    assertTrue(newPopupAction().isEnabledInModalContext)
  }

  @Test
  fun `is dump aware`() {
    assertTrue(newPopupAction().isDumbAware)
  }

  private fun newPopupAction() =
    PopupAction(action(), mock(), IdePopupManager(), mock(), mock())
}
