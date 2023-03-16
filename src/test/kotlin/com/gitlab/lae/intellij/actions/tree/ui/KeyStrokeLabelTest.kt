package com.gitlab.lae.intellij.actions.tree.ui

import java.awt.event.KeyEvent.VK_BACK_QUOTE
import java.awt.event.KeyEvent.VK_BACK_SLASH
import java.awt.event.KeyEvent.VK_CLOSE_BRACKET
import java.awt.event.KeyEvent.VK_COMMA
import java.awt.event.KeyEvent.VK_EQUALS
import java.awt.event.KeyEvent.VK_MINUS
import java.awt.event.KeyEvent.VK_OPEN_BRACKET
import java.awt.event.KeyEvent.VK_PERIOD
import java.awt.event.KeyEvent.VK_QUOTE
import java.awt.event.KeyEvent.VK_SEMICOLON
import java.awt.event.KeyEvent.VK_SLASH
import javax.swing.KeyStroke
import javax.swing.KeyStroke.getKeyStroke
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class KeyStrokeLabelTest(
    private val expected: String,
    private val keyStroke: KeyStroke,
) {

  @Test
  fun test() {
    assertEquals(expected, getKeyText(keyStroke))
  }

  companion object {

    @JvmStatic
    @Parameters(name = "{0}, {1}")
    fun data() =
        arrayOf(
            arrayOf("`", getKeyStroke(VK_BACK_QUOTE, 0)),
            arrayOf("\\", getKeyStroke(VK_BACK_SLASH, 0)),
            arrayOf("[", getKeyStroke(VK_OPEN_BRACKET, 0)),
            arrayOf("]", getKeyStroke(VK_CLOSE_BRACKET, 0)),
            arrayOf(",", getKeyStroke(VK_COMMA, 0)),
            arrayOf("=", getKeyStroke(VK_EQUALS, 0)),
            arrayOf("-", getKeyStroke(VK_MINUS, 0)),
            arrayOf(".", getKeyStroke(VK_PERIOD, 0)),
            arrayOf("'", getKeyStroke(VK_QUOTE, 0)),
            arrayOf(";", getKeyStroke(VK_SEMICOLON, 0)),
            arrayOf("/", getKeyStroke(VK_SLASH, 0)),
            arrayOf("@", getKeyStroke('@')),
            arrayOf("<", getKeyStroke('<')),
            arrayOf("|", getKeyStroke('|')),
        )
  }
}
