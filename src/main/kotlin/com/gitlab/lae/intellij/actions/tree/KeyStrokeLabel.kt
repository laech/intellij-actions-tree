package com.gitlab.lae.intellij.actions.tree

import com.intellij.openapi.keymap.KeymapUtil.getKeyText
import com.intellij.openapi.keymap.KeymapUtil.getKeystrokeText
import com.intellij.util.ui.JBUI
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
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

    fun setForeground(fg: Color) {
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

        val text = getKeystrokeText(key)
        if (getKeyText(key.keyCode).length > 1) {
            first.text = text
            panel.add(first)
            return
        }

        val prefix = text.dropLast(1)
        first.text = prefix
        panel.add(first)

        val ms = second.getFontMetrics(second.font)
        second.text = text.substring(prefix.length)
        second.preferredSize = Dimension(ms.stringWidth("W"), ms.height)
        panel.add(second)
    }
}
