package com.gitlab.lae.intellij.actions.tree

import com.intellij.ui.SeparatorWithText
import com.intellij.util.ui.EmptyIcon
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Component
import java.awt.FlowLayout
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListCellRenderer
import javax.swing.border.EmptyBorder

class ActionPresentationRenderer : ListCellRenderer<ActionPresentation> {

    private val separator = SeparatorWithText()
    private val root = JPanel(BorderLayout())
    private val content = JPanel(BorderLayout(30, 0))
    private val nameLabel = JLabel()

    private val keyLabels = mutableListOf<KeyStrokeLabel>()
    private val keyLabelsPanel = JPanel(FlowLayout(FlowLayout.TRAILING, 0, 0))

    private var emptyIconInit = false
    private var emptyIcon: EmptyIcon? = null

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

        initEmptyIcon(list)

        separator.isVisible = value.separatorAbove

        while (keyLabels.size < value.keys.size) {
            keyLabels.add(KeyStrokeLabel())
        }

        setColors(list, isSelected)

        val p = value.presentation
        nameLabel.isEnabled = p.isEnabled
        nameLabel.text = p.text
        nameLabel.disabledIcon = p.disabledIcon ?: emptyIcon
        nameLabel.icon = (if (isSelected) p.selectedIcon else p.icon)
                ?: p.icon ?: emptyIcon

        keyLabelsPanel.removeAll()
        value.keys.forEachIndexed { i, key ->
            val label = keyLabels[keyLabels.size - i - 1]
            label.setEnabled(p.isEnabled)
            label.setTextFromKeyStroke(key)
            keyLabelsPanel.add(label.component)
        }

        return root
    }

    private fun initEmptyIcon(list: JList<out ActionPresentation>) {
        if (emptyIconInit) {
            return
        }
        emptyIconInit = true
        (0 until list.model.size).map(list.model::getElementAt).find g@{
            val p = it.presentation
            val icon = p.icon ?: p.disabledIcon ?: p.selectedIcon
            ?: return@g false
            emptyIcon = EmptyIcon.create(icon.iconWidth, icon.iconHeight)
            true
        }
    }

    private fun setColors(list: JList<out ActionPresentation>, isSelected: Boolean) {
        if (isSelected) {
            content.background = list.selectionBackground
            nameLabel.foreground = list.selectionForeground
            keyLabels.forEach { it.setForeground(list.selectionForeground) }
        } else {
            content.background = list.background
            nameLabel.foreground = list.foreground
            keyLabels.forEach { it.setForeground(list.foreground) }
        }
    }

}
