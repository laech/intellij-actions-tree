package com.gitlab.lae.intellij.actions.tree

import com.intellij.openapi.ui.popup.*
import com.intellij.util.ui.StatusText
import java.awt.Component
import javax.swing.Icon

class ActionStep(
        private val component: Component?,
        private val root: ActionGroup
) : ListPopupStepEx<ActionNode> {

    override fun setEmptyText(emptyText: StatusText) {}

    override fun isSelectable(value: ActionNode) =
            value.templatePresentation.isEnabled

    override fun getDefaultOptionIndex() = -1

    override fun getSeparatorAbove(value: ActionNode): ListSeparator? = null

    override fun isAutoSelectionEnabled() = false

    override fun getFinalRunnable(): Runnable? = null

    override fun canceled() {}

    override fun isMnemonicsNavigationEnabled() = false

    override fun getMnemonicNavigationFilter(): MnemonicNavigationFilter<ActionNode>? = null

    override fun isSpeedSearchEnabled() = false

    override fun getSpeedSearchFilter(): SpeedSearchFilter<ActionNode>? = null

    override fun getValues() = root.items

    override fun hasSubstep(selectedValue: ActionNode) =
            selectedValue is ActionGroup

    override fun onChosen(selectedValue: ActionNode, finalChoice: Boolean) =
            onChosen(selectedValue, finalChoice, 0)

    override fun onChosen(
            selectedValue: ActionNode,
            finalChoice: Boolean,
            modifiers: Int
    ): PopupStep<*>? {
        selectedValue.performAction(component, modifiers)
        return null
    }

    override fun getTitle(): String? = null

    override fun getTextFor(value: ActionNode) =
            value.templatePresentation.text
                    ?: when (value) {
                        is ActionGroup -> "..."
                        is ActionRef -> value.id
                    }

    override fun getTooltipTextFor(value: ActionNode): String? =
            value.templatePresentation.description

    override fun getIconFor(value: ActionNode): Icon? =
            value.templatePresentation.run {
                if (isEnabled) icon else disabledIcon
            }

}
