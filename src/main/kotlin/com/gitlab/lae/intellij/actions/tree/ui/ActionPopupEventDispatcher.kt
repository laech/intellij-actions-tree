package com.gitlab.lae.intellij.actions.tree.ui

import com.intellij.ide.IdePopupManager
import com.intellij.openapi.ui.popup.IdePopupEventDispatcher
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupListener
import com.intellij.openapi.ui.popup.LightweightWindowEvent
import java.awt.AWTEvent
import java.awt.Component
import java.awt.event.KeyEvent
import java.util.stream.Stream

/**
 * Using an [IdePopupEventDispatcher] is required to let IntelliJ
 * know we have a popup showing, therefore keyboard events should
 * be forward to us for processing first.
 *
 * For example, without this if the popup has an item in the list
 * with shortcut "Ctrl+D", and there is a global shortcut specified
 * in the IntelliJ keymap for "Ctrl+D Ctrl+X" (2 key strokes),
 * then when "Ctrl+D" is pressed while the popup is showing,
 * the entry in the popup won't be executed, instead IntelliJ will
 * go into waiting for the second key stroke, since it sees "Ctrl+D"
 * as a prefix key. We want the entry in the popup to be execute
 * when "Ctrl+D" is pressed.
 */
class ActionPopupEventDispatcher(
  private val popup: JBPopup,
  private val list: ActionList,
  private val popupManager: IdePopupManager,
) : IdePopupEventDispatcher, JBPopupListener {

  override fun getPopupStream(): Stream<JBPopup> = Stream.of(popup)

  override fun dispatch(event: AWTEvent): Boolean {
    if (event !is KeyEvent) {
      return false
    }
    list.processKeyEvent(event)
    return event.isConsumed
  }

  override fun getComponent(): Component = popup.content

  override fun requestFocus(): Boolean {
    popup.content.requestFocus()
    return true
  }

  override fun close(): Boolean {
    popup.closeOk(null)
    return true
  }

  override fun beforeShown(event: LightweightWindowEvent) {
    popupManager.push(this)
  }

  override fun onClosed(event: LightweightWindowEvent) {
    popupManager.remove(this)
  }
}
