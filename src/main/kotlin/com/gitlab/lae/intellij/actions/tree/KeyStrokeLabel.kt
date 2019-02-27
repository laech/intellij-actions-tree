package com.gitlab.lae.intellij.actions.tree

import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.util.ui.JBUI
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.*
import javax.swing.*

/**
 * Make keystroke labels look vertically aligned better
 * when proportional font is used.
 *
 * For example, when "^F" and "^B" appears above/below each
 * other in the popup, because "F" and "B" and different fonts widths,
 * they look unaligned and out of place. This looks worst when multiple
 * keystrokes are assigned to and item.
 */
class KeyStrokeLabel {

    private val panel = JPanel(FlowLayout(FlowLayout.TRAILING, 0, 0))
    private val first = JLabel(null as String?, SwingConstants.TRAILING)
    private val second = JLabel(null as String?, SwingConstants.CENTER)

    val component: JComponent
        get() = panel

    init {
        first.background = null
        second.background = null
        panel.add(first)
        panel.add(second)
        panel.background = null
        panel.border = JBUI.Borders.emptyLeft(5)
    }

    fun setForeground(fg: Color?) {
        first.foreground = fg
        second.foreground = fg
    }

    fun setEnabled(enabled: Boolean) {
        first.isEnabled = enabled
        second.isEnabled = enabled
    }

    fun setTextFromKeyStroke(key: KeyStroke?) {

        first.preferredSize = null
        second.preferredSize = null
        panel.removeAll()

        key ?: return

        val (prefix, suffix) = getKeyTextParts(key)
        first.text = prefix
        panel.add(first)

        second.text = suffix
        second.preferredSize = if (suffix.length > 1) {
            null
        } else {
            val ms = second.getFontMetrics(second.font)
            Dimension(Integer.max(ms.stringWidth("W"), ms.stringWidth("@")), ms.height)
        }
        panel.add(second)
    }
}

fun getKeyText(key: KeyStroke): String {
    val (prefix, suffix) = getKeyTextParts(key)
    return "$prefix$suffix"
}

private fun getKeyTextParts(key: KeyStroke): Pair<String, String> {
    val copy = KeyStroke.getKeyStroke(KeyEvent.VK_A, key.modifiers, key.isOnKeyRelease)
    val prefix = KeymapUtil.getKeystrokeText(copy).dropLast(1)
    val suffix = if (key.keyChar != CHAR_UNDEFINED) {
        "${key.keyChar}"
    } else {
        getKeyText(key.keyCode)
    }
    return Pair(prefix, suffix)
}

private fun getKeyText(keyCode: Int): String = when (keyCode) {
    VK_MINUS -> "-"
    VK_COMMA -> ","
    VK_QUOTE -> "'"
    else -> KeymapUtil.getKeyText(keyCode)
}