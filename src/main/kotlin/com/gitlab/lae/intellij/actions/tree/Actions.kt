package com.gitlab.lae.intellij.actions.tree

import com.gitlab.lae.intellij.actions.tree.popup.ActionItem
import com.gitlab.lae.intellij.actions.tree.popup.ActionPopup
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys.CONTEXT_COMPONENT
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.ui.popup.ListSeparator
import com.intellij.openapi.util.AsyncResult
import com.intellij.util.Consumer
import java.awt.Component
import javax.swing.KeyStroke

sealed class ActionNode {
    abstract val keys: List<KeyStroke>
}

data class ActionRef(
        override val keys: List<KeyStroke>,
        val id: String,
        val header: String?) : ActionNode()

data class ActionGroup(
        override val keys: List<KeyStroke>,
        val items: List<ActionNode>) : ActionNode()

fun ActionNode.toAction(mgr: ActionManager): AnAction? = when (this) {
    is ActionRef -> mgr.getAction(id)
    is ActionGroup -> object : AnAction("...") { // TODO
        override fun actionPerformed(e: AnActionEvent) = showPopup(e)
        override fun isDumbAware() = true
    }
}

private fun ActionGroup.showPopup(e: AnActionEvent) {
    val actions = items.mapNotNull { it.toActionItem(e) }
    val component = e.dataContext.getData(CONTEXT_COMPONENT)
    val popup = ActionPopup(component, actions)
    actions.forEach { (action, keys) ->
        popup.registerKeyboardAction(keys) { e ->
            popup.closeOk(null)
            action.performAction(component, e.modifiers)
        }
    }
    popup.showInBestPositionFor(e.dataContext)
}

fun ActionNode.toActionItem(src: AnActionEvent): ActionItem? {
    val action = toAction(src.actionManager) ?: return null
    val presentation = action.templatePresentation.clone()
    val place = ActionPlaces.EDITOR_POPUP
    val input = src.inputEvent
    val ctx = src.dataContext
    action.update(AnActionEvent.createFromAnAction(action, input, place, ctx))
    val hasChildren = this is ActionGroup
    val separator = if (this is ActionRef && header != null) {
        ListSeparator(header)
    } else {
        null
    }
    return ActionItem(
            action,
            keys,
            presentation.text,
            presentation.description,
            presentation.isEnabled,
            hasChildren,
            separator)
}

fun AnAction.performAction(component: Component?, modifiers: Int) {
    val dataManager = DataManager.getInstance()
    when (component) {
        null -> dataManager.dataContextFromFocus
        else -> AsyncResult.done(dataManager.getDataContext(component))
    }.doWhenDone(Consumer {
        val actions = ActionManager.getInstance()
        val template = templatePresentation.clone()
        val place = ActionPlaces.EDITOR_POPUP
        val event = AnActionEvent(null, it, place, template, actions, modifiers)
        event.setInjectedContext(isInInjectedContext)
        if (ActionUtil.lastUpdateAndCheckDumb(this, event, false)) {
            ActionUtil.performActionDumbAware(this, event)
        }
    })
}
