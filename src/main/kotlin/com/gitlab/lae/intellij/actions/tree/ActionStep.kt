package com.gitlab.lae.intellij.actions.tree

import com.intellij.openapi.ui.popup.ListPopupStep
import com.intellij.openapi.ui.popup.ListPopupStepEx
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.util.ui.StatusText

class ActionStep<T>(private val step: ListPopupStep<T>)
    : ListPopupStep<T> by step, ListPopupStepEx<T> {

    override fun isSpeedSearchEnabled() = false
    override fun isAutoSelectionEnabled() = false
    override fun isMnemonicsNavigationEnabled() = false
    override fun setEmptyText(emptyText: StatusText) {}

    override fun getTooltipTextFor(value: T) =
            (step as? ListPopupStepEx<T>)?.getTooltipTextFor(value)

    override fun onChosen(value: T, final: Boolean, modifiers: Int): PopupStep<*>? =
            if (step is ListPopupStepEx<T>) {
                step.onChosen(value, final, modifiers)
            } else {
                onChosen(value, final)
            }
}
