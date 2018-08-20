package com.gitlab.lae.intellij.actions.tree

import com.intellij.openapi.ui.popup.ListPopupStepEx
import com.intellij.openapi.ui.popup.MnemonicNavigationFilter
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.SpeedSearchFilter
import com.intellij.util.ui.StatusText
import java.awt.Component
import javax.swing.Icon

class ActionStep(
        private val component: Component?,
        private val items: List<ActionPresentation>
) : ListPopupStepEx<ActionPresentation> {

    override fun setEmptyText(emptyText: StatusText) {}

    override fun isSelectable(value: ActionPresentation) = value.isEnabled

    override fun getDefaultOptionIndex() = -1

    override fun getSeparatorAbove(value: ActionPresentation) = value.separator

    override fun isAutoSelectionEnabled() = false

    override fun getFinalRunnable(): Runnable? = null

    override fun canceled() {}

    override fun isMnemonicsNavigationEnabled() = false

    override fun getMnemonicNavigationFilter(): MnemonicNavigationFilter<ActionPresentation>? = null

    override fun isSpeedSearchEnabled() = false

    override fun getSpeedSearchFilter(): SpeedSearchFilter<ActionPresentation>? = null

    override fun getValues() = items

    override fun hasSubstep(selectedValue: ActionPresentation) = selectedValue.hasChildren

    override fun onChosen(selectedValue: ActionPresentation, finalChoice: Boolean) =
            onChosen(selectedValue, finalChoice, 0)

    override fun onChosen(
            selectedValue: ActionPresentation,
            finalChoice: Boolean,
            modifiers: Int
    ): PopupStep<*>? {
        selectedValue.action.performAction(component, modifiers)
        return null
    }

    override fun getTitle(): String? = null

    override fun getTextFor(value: ActionPresentation) = value.name ?: ""

    override fun getTooltipTextFor(value: ActionPresentation) = value.description

    override fun getIconFor(value: ActionPresentation): Icon? = null

    override fun getSelectedIconFor(value: ActionPresentation): Icon? = null

}
