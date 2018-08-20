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
import javax.swing.JComponent
import javax.swing.KeyStroke

interface ActionNode {

    val keys: List<KeyStroke>

    fun toPresentation(src: AnActionEvent): ActionItem? {
        val action = toAction(src.actionManager) ?: return null
        val presentation = action.templatePresentation.clone()
        val place = ActionPlaces.EDITOR_POPUP
        val input = src.inputEvent
        val ctx = src.dataContext
        action.update(AnActionEvent.createFromAnAction(action, input, place, ctx))
        return ActionItem(
                action,
                keys,
                presentation.text,
                presentation.description,
                this is ActionGroup,
                presentation.isEnabled,
                if (this is ActionRef && separatorAbove) ListSeparator()
                else null)
    }

    fun toAction(mgr: ActionManager): AnAction?
}

data class ActionRef(
        override val keys: List<KeyStroke>,
        val id: String,
        val separatorAbove: Boolean) : ActionNode {

    override fun toAction(mgr: ActionManager): AnAction? =
            mgr.getAction(id)
}

data class ActionGroup(
        override val keys: List<KeyStroke>,
        val items: List<ActionNode>) : ActionNode {

    override fun toAction(mgr: ActionManager) = object : AnAction("...") { // TODO
        override fun actionPerformed(e: AnActionEvent) = run(e)
        override fun isDumbAware() = true
    }

    private fun run(e: AnActionEvent) {
        val actions = items.asSequence()
                .map { it.toPresentation(e) }
                .filterNotNull()
                .toList()

        val component = e.dataContext.getData(CONTEXT_COMPONENT)
        val popup = ActionPopup(component, actions)

        actions.forEach { (action, keys, _, _, _) ->
            keys.forEach { key ->
                popup.content.registerKeyboardAction({ e ->
                    popup.closeOk(null)
                    action.performAction(component, e.modifiers)
                }, key, JComponent.WHEN_IN_FOCUSED_WINDOW)
            }
        }

        popup.showInBestPositionFor(e.dataContext)
    }
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
