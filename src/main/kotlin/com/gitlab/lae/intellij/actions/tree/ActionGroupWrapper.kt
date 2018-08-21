package com.gitlab.lae.intellij.actions.tree

import com.intellij.openapi.actionSystem.*
import javax.swing.KeyStroke

open class ActionGroupWrapper(
        keys: List<KeyStroke>,
        private val action: ActionGroup
) : ActionGroup() {

    init {
        copyFrom(action)
        super.setShortcutSet(CustomShortcutSet(*keys.map {
            KeyboardShortcut(it, null)
        }.toTypedArray()))
    }

    override fun setShortcutSet(shortcutSet: ShortcutSet) {}

    override fun actionPerformed(e: AnActionEvent) = action.actionPerformed(e)
    override fun beforeActionPerformedUpdate(e: AnActionEvent) {
        action.beforeActionPerformedUpdate(e)
        copyFrom(action)
    }

    override fun update(e: AnActionEvent) {
        action.update(e)
        copyFrom(action)
    }

    override fun setDefaultIcon(isDefaultIconSet: Boolean) {
        action.isDefaultIcon = isDefaultIconSet
    }

    override fun setInjectedContext(worksInInjected: Boolean) =
            action.setInjectedContext(worksInInjected)

    override fun displayTextInToolbar() = action.displayTextInToolbar()
    override fun startInTransaction() = action.startInTransaction()
    override fun isInInjectedContext() = action.isInInjectedContext
    override fun isTransparentUpdate() = action.isTransparentUpdate
    override fun isDefaultIcon() = action.isDefaultIcon
    override fun isDumbAware() = action.isDumbAware

    override fun isPopup() = action.isPopup

    override fun hideIfNoVisibleChildren() =
            action.hideIfNoVisibleChildren()

    override fun disableIfNoVisibleChildren() =
            action.disableIfNoVisibleChildren()

    override fun canBePerformed(context: DataContext) =
            action.canBePerformed(context)

    override fun getChildren(e: AnActionEvent?): Array<out AnAction> =
            action.getChildren(e)
}
