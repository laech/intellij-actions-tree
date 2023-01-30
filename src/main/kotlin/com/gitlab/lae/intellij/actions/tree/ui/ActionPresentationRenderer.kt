package com.gitlab.lae.intellij.actions.tree.ui

import com.intellij.ui.SeparatorWithText
import com.intellij.util.ui.EmptyIcon
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListCellRenderer
import javax.swing.UIManager
import javax.swing.border.EmptyBorder

class ActionPresentationRenderer : ListCellRenderer<ActionPresentation> {

  private val separator = SeparatorWithText()
  private val root = JPanel(BorderLayout())
  private val content = JPanel(BorderLayout(30, 0))
  private val nameLabel = JLabel()
  private val keyLabel = JLabel()
  private var emptyIconInit = false
  private var emptyIcon: EmptyIcon? = null

  init {
    nameLabel.background = null
    content.add(nameLabel, BorderLayout.CENTER)
    content.add(keyLabel, BorderLayout.LINE_END)
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
    cellHasFocus: Boolean,
  ): Component {

    initEmptyIcon(list)
    separator.caption = value.separatorAbove
    separator.isVisible = value.separatorAbove != null

    setColors(list, isSelected)

    val presentation = value.presentation
    nameLabel.isEnabled = presentation.isEnabled
    nameLabel.text = presentation.text
    nameLabel.disabledIcon =
      if (presentation.disabledIcon != null) presentation.disabledIcon
      else emptyIcon

    val icon =
      (if (isSelected) presentation.selectedIcon else presentation.icon)
        ?: presentation.icon
        ?: emptyIcon

    nameLabel.icon = icon
    keyLabel.isEnabled = presentation.isEnabled
    keyLabel.text = value.keys.joinToString(", ") { getKeyText(it) }
    return root
  }

  private fun initEmptyIcon(list: JList<out ActionPresentation>) {
    if (emptyIconInit) {
      return
    }
    emptyIconInit = true

    emptyIcon = generateSequence(0, Int::inc)
      .take(list.model.size)
      .map {
        val p = list.model.getElementAt(it).presentation
        val icon = p.icon ?: p.disabledIcon ?: p.selectedIcon ?: return@map null
        EmptyIcon.create(
          icon.iconWidth,
          icon.iconHeight,
        )
      }
      .filterNotNull()
      .firstOrNull()
  }

  private fun setColors(
    list: JList<out ActionPresentation>,
    isSelected: Boolean,
  ) {
    if (isSelected) {
      content.background = list.selectionBackground
      nameLabel.foreground = list.selectionForeground
      keyLabel.foreground = UIManager.getColor(
        "MenuItem.acceleratorSelectionForeground",
      )
    } else {
      content.background = list.background
      nameLabel.foreground = list.foreground
      keyLabel.foreground = UIManager.getColor(
        "MenuItem.acceleratorForeground",
      )
    }
  }
}
