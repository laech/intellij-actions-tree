package com.gitlab.lae.intellij.actions.tree

import com.intellij.ide.DataManager
import com.intellij.ide.IdePopupManager
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.IdeFocusManager
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

class PopupActionTest {

  @Test
  fun `enabled in modal context`() {
    assertTrue(newPopupAction().isEnabledInModalContext)
  }

  @Test
  fun `is dump aware`() {
    assertTrue(newPopupAction().isDumbAware)
  }

  private fun newPopupAction() = PopupAction(
    ActionNode("", "", "", false, When.ALWAYS, emptyList(), emptyList()),
    mock(IdeFocusManager::class.java),
    IdePopupManager(),
    mock(JBPopupFactory::class.java),
    mock(DataManager::class.java)
  )
}
