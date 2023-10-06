package com.gitlab.lae.intellij.actions.tree.ui

import com.intellij.ide.IdePopupManager
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.CHAR_UNDEFINED
import java.awt.event.KeyEvent.VK_A
import javax.swing.JLabel
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.mock

class ActionPopupEventDispatcherTest {

  @Test
  fun forwardsKeyEvent() {
    val list = mock<ActionList>()
    val dispatcher = ActionPopupEventDispatcher(mock(), list, IdePopupManager())
    val event = KeyEvent(JLabel(), 0, 0, 0, VK_A, CHAR_UNDEFINED)
    dispatcher.dispatch(event)
    verify(list).processKeyEvent(event)
  }

  @Test
  fun informsIdePopupManagerOfPopupState() {
    val idePopupManager = IdePopupManager()
    val dispatcher = ActionPopupEventDispatcher(mock(), mock(), idePopupManager)
    val myDispatchStack = getDispatchStack(idePopupManager)
    assertFalse(myDispatchStack.contains(dispatcher))

    dispatcher.beforeShown(mock())
    assertTrue(myDispatchStack.contains(dispatcher))

    dispatcher.onClosed(mock())
    assertFalse(myDispatchStack.contains(dispatcher))
  }

  private fun getDispatchStack(manager: IdePopupManager): List<*> {
    val myDispatchStackField = IdePopupManager::class.java.getDeclaredField("myDispatchStack")
    myDispatchStackField.isAccessible = true
    return myDispatchStackField[manager] as List<*>
  }
}
