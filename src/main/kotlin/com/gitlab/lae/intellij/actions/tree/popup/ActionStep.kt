package com.gitlab.lae.intellij.actions.tree.popup

import com.gitlab.lae.intellij.actions.tree.performAction
import com.intellij.openapi.ui.popup.ListPopupStepEx
import com.intellij.openapi.ui.popup.MnemonicNavigationFilter
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.SpeedSearchFilter
import com.intellij.util.ui.StatusText
import java.awt.Component
import javax.swing.Icon

class ActionStep(
        private val component: Component?,
        private val items: List<ActionItem>
) : ListPopupStepEx<ActionItem> {

    override fun getValues() = items
    override fun getTitle(): String? = null
    override fun getDefaultOptionIndex() = -1
    override fun setEmptyText(emptyText: StatusText) {}

    override fun isAutoSelectionEnabled() = false
    override fun isMnemonicsNavigationEnabled() = false
    override fun isSpeedSearchEnabled() = false

    override fun getMnemonicNavigationFilter(): MnemonicNavigationFilter<ActionItem>? = null
    override fun getSpeedSearchFilter(): SpeedSearchFilter<ActionItem>? = null
    override fun getFinalRunnable(): Runnable? = null
    override fun canceled() {}

    override fun hasSubstep(value: ActionItem) = value.hasChildren
    override fun isSelectable(value: ActionItem) = value.isEnabled
    override fun getSeparatorAbove(value: ActionItem) = value.separator

    override fun getTextFor(value: ActionItem) = value.name ?: ""
    override fun getIconFor(value: ActionItem): Icon? = null
    override fun getSelectedIconFor(value: ActionItem): Icon? = null
    override fun getTooltipTextFor(value: ActionItem) = value.description

    override fun onChosen(value: ActionItem, final: Boolean) = onChosen(value, final, 0)
    override fun onChosen(value: ActionItem, final: Boolean, modifiers: Int): PopupStep<*>? {
        value.action.performAction(component, modifiers)
        return null
    }

}
