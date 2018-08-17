package com.gitlab.lae.intellij.actions.tree

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.ui.popup.*
import com.intellij.util.ui.StatusText
import java.awt.Component
import javax.swing.Icon

class ActionStep(
        private val component: Component?,
        private val root: ActionNode
) : ListPopupStepEx<AnAction> {

    override fun setEmptyText(emptyText: StatusText) {}

    override fun isSelectable(value: AnAction) =
            value.templatePresentation.isEnabled

    override fun getDefaultOptionIndex() = -1

    override fun getSeparatorAbove(value: AnAction): ListSeparator? = null

    override fun isAutoSelectionEnabled() = false

    override fun getFinalRunnable(): Runnable? = null

    override fun canceled() {}

    override fun isMnemonicsNavigationEnabled() = false

    override fun getMnemonicNavigationFilter(): MnemonicNavigationFilter<AnAction>? = null

    override fun isSpeedSearchEnabled() = false

    override fun getSpeedSearchFilter(): SpeedSearchFilter<AnAction>? = null

    override fun getValues() = root.items

    override fun hasSubstep(selectedValue: AnAction) = selectedValue is ActionNode

    override fun onChosen(selectedValue: AnAction, finalChoice: Boolean) =
            onChosen(selectedValue, finalChoice, 0)

    override fun onChosen(selectedValue: AnAction, finalChoice: Boolean, modifiers: Int): PopupStep<*>? {
        selectedValue.performAction(component, modifiers)
        return null
    }

    override fun getTitle(): String? = null

    override fun getTextFor(value: AnAction) =
            value.templatePresentation.text ?: ""

    override fun getTooltipTextFor(value: AnAction): String? =
            value.templatePresentation.description

    override fun getIconFor(value: AnAction): Icon? =
            value.templatePresentation.run {
                if (isEnabled) icon else disabledIcon
            }

}
