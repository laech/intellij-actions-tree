package com.gitlab.lae.intellij.actions.tree

import com.intellij.ide.IdePopupManager
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock

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
