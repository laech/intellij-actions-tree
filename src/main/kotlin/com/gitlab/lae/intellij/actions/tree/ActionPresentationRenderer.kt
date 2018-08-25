package com.gitlab.lae.intellij.actions.tree

import com.intellij.ui.SeparatorWithText
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Component
import java.awt.FlowLayout
import javax.swing.*
import javax.swing.border.EmptyBorder

class ActionPresentationRenderer : ListCellRenderer<ActionPresentation> {

    private val separator = SeparatorWithText()
    private val root = JPanel(BorderLayout())
    private val content = JPanel(BorderLayout(30, 0))
    private val nameLabel = JLabel()
    private val keyLabels = mutableListOf<KeyStrokeLabel>()
    private val keyLabelsPanel = JPanel(FlowLayout(FlowLayout.TRAILING, 0, 0))

    init {

        nameLabel.background = null
        keyLabelsPanel.background = null

        content.add(nameLabel, BorderLayout.CENTER)
        content.add(keyLabelsPanel, BorderLayout.LINE_END)
        content.border = EmptyBorder(UIUtil.getListCellPadding())

        root.background = null
        root.add(separator, BorderLayout.PAGE_START)
        root.add(content, BorderLayout.CENTER)
    }

    override fun getListCellRendererComponent(
            list: JList<out ActionPresentation>,
            value: ActionPresentation,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
    ): Component {

        separator.isVisible = value.separatorAbove

        while (keyLabels.size < value.keys.size) {
            keyLabels.add(KeyStrokeLabel())
        }

        setColors(list, isSelected)

        nameLabel.isEnabled = value.presentation.isEnabled
        nameLabel.text = value.presentation.text

        keyLabelsPanel.removeAll()
        value.keys.forEachIndexed { i, key ->
            val label = keyLabels[keyLabels.size - i - 1]
            label.setEnabled(value.presentation.isEnabled)
            label.setTextFromKeyStroke(key)
            keyLabelsPanel.add(label.component)
        }

        return root
    }

    private fun setColors(list: JList<out ActionPresentation>, isSelected: Boolean) {
        if (isSelected) {
            content.background = list.selectionBackground
            nameLabel.foreground = list.selectionForeground
            keyLabels.forEach {
                it.setForeground(UIManager.getColor(
                        "MenuItem.acceleratorSelectionForeground"))
            }
        } else {
            content.background = list.background
            nameLabel.foreground = list.foreground
            keyLabels.forEach {
                it.setForeground(UIManager.getColor(
                        "MenuItem.acceleratorForeground"))
            }
        }
    }

}
