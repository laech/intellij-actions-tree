package com.gitlab.lae.intellij.actions.tree.ui

import com.intellij.openapi.keymap.KeymapUtil
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.CHAR_UNDEFINED
import javax.swing.KeyStroke

fun getKeyText(key: KeyStroke): String {
  val copy = KeyStroke.getKeyStroke(
    KeyEvent.VK_A,
    key.modifiers,
    key.isOnKeyRelease
  )

  var prefix = KeymapUtil.getKeystrokeText(copy)
  prefix = prefix.substring(0, prefix.length - 1)

  val suffix =
    if (key.keyChar != CHAR_UNDEFINED) key.keyChar.toString()
    else getKeyText(key.keyCode)

  return prefix + suffix
}

private fun getKeyText(keyCode: Int): String = when (keyCode) {
  KeyEvent.VK_MINUS -> "-"
  KeyEvent.VK_COMMA -> ","
  KeyEvent.VK_QUOTE -> "'"
  else -> KeymapUtil.getKeyText(keyCode)
}
