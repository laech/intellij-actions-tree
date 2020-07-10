package com.gitlab.lae.intellij.actions.tree.ui

import com.intellij.ide.IdePopupManager
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.verify
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.CHAR_UNDEFINED
import java.awt.event.KeyEvent.VK_A
import javax.swing.JLabel

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
