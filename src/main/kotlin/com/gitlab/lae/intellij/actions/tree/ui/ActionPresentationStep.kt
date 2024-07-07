package com.gitlab.lae.intellij.actions.tree.ui

import com.intellij.openapi.ui.popup.ListPopupStepEx
import com.intellij.openapi.ui.popup.ListSeparator
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseStep
import com.intellij.openapi.util.IconLoader
import com.intellij.util.ui.EmptyIcon
import com.intellij.util.ui.StatusText
import java.awt.event.InputEvent

class ActionPresentationStep(
  private val values: Collection<ActionPresentation>,
  private val action: (ActionPresentation, Int) -> Unit,
) : BaseStep<ActionPresentation>(), ListPopupStepEx<ActionPresentation> {

  private val emptyIcon =
    values.firstNotNullOfOrNull {
      (it.presentation.icon ?: it.presentation.disabledIcon ?: it.presentation.selectedIcon)?.run {
        EmptyIcon.create(iconWidth, iconHeight)
      }
    }

  override fun getTitle() = null

  override fun canceled() = Unit

  override fun getValues() = values.toMutableList()

  override fun getDefaultOptionIndex() = -1

  override fun setEmptyText(emptyText: StatusText) = Unit

  override fun getTooltipTextFor(value: ActionPresentation) = null

  override fun getSeparatorAbove(value: ActionPresentation) =
    value.separatorAbove?.let { ListSeparator(it) }

  override fun getIconFor(value: ActionPresentation) =
    if (!isSelectable(value)) {
      value.presentation.disabledIcon ?: value.presentation.icon?.let(IconLoader::getDisabledIcon)
    } else {
      value.presentation.icon
    } ?: emptyIcon

  override fun getSelectedIconFor(value: ActionPresentation) =
    value.presentation.selectedIcon ?: getIconFor(value)

  override fun isSelectable(value: ActionPresentation) = value.presentation.isEnabled

  override fun getTextFor(value: ActionPresentation): String = value.presentation.getText(false)

  override fun hasSubstep(selectedValue: ActionPresentation) = false

  override fun onChosen(selectedValue: ActionPresentation, finalChoice: Boolean): PopupStep<*>? {
    action(selectedValue, 0)
    return null
  }

  override fun onChosen(
    selectedValue: ActionPresentation,
    finalChoice: Boolean,
    inputEvent: InputEvent?,
  ): PopupStep<*>? {
    action(selectedValue, inputEvent?.modifiersEx ?: 0)
    return null
  }
}
