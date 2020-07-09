package com.gitlab.lae.intellij.actions.tree.ui

import com.intellij.ide.IdePopupManager
import com.intellij.openapi.ui.popup.JBPopup
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.CHAR_UNDEFINED
import java.awt.event.KeyEvent.VK_A
import javax.swing.JLabel

class ActionPopupEventDispatcherTest {

  @Test
  fun forwardsKeyEvent() {
    val list = mock(ActionList::class.java)
    val dispatcher = ActionPopupEventDispatcher(
      mock(JBPopup::class.java),
      list,
      IdePopupManager()
    )
    val event = KeyEvent(JLabel(), 0, 0, 0, VK_A, CHAR_UNDEFINED)
    dispatcher.dispatch(event)
    verify(list).processKeyEvent(event)
  }

  @Test
  fun informsIdePopupManagerOfPopupState() {
    val idePopupManager = IdePopupManager()
    val dispatcher = ActionPopupEventDispatcher(
      mock(JBPopup::class.java),
      mock(ActionList::class.java),
      idePopupManager
    )

    val myDispatchStack = getDispatchStack(idePopupManager)
    assertFalse(myDispatchStack.contains(dispatcher))

    dispatcher.beforeShown(null)
    assertTrue(myDispatchStack.contains(dispatcher))

    dispatcher.onClosed(null)
    assertFalse(myDispatchStack.contains(dispatcher))
  }

  private fun getDispatchStack(manager: IdePopupManager): List<*> {
    val myDispatchStackField =
      IdePopupManager::class.java.getDeclaredField("myDispatchStack")
    myDispatchStackField.isAccessible = true
    return myDispatchStackField[manager] as List<*>
  }
}
