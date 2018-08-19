package com.gitlab.lae.intellij.actions.tree

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.IdeActions.*
import com.intellij.openapi.actionSystem.PlatformDataKeys.CONTEXT_COMPONENT
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.keymap.KeymapManager
import com.intellij.openapi.util.AsyncResult
import com.intellij.ui.popup.list.ListPopupImpl
import com.intellij.util.Consumer
import java.awt.Component
import javax.swing.JComponent
import javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW
import javax.swing.JList
import javax.swing.KeyStroke
import javax.swing.ListCellRenderer

interface ActionNode : ShortcutProvider {

    val keys: List<KeyStroke>

    fun toPresentation(src: AnActionEvent): ActionPresentation? {
        val action = toAction(src.actionManager) ?: return null
        val presentation = action.templatePresentation.clone()
        val place = ActionPlaces.EDITOR_POPUP
        val input = src.inputEvent
        val ctx = src.dataContext
        action.update(AnActionEvent.createFromAnAction(action, input, place, ctx))
        val result = ActionPresentation(action, keys, presentation)
        when (this) {
            is ActionGroup -> result.hasSubstep = true
            is ActionRef -> result.sepAbove = this.sepAbove
        }
        return result
    }

    fun toAction(mgr: ActionManager): AnAction?

    override fun getShortcut() = CustomShortcutSet(*keys
            .map { KeyboardShortcut(it, null) }
            .toTypedArray())
}

data class ActionRef(
        override val keys: List<KeyStroke>,
        val id: String,
        val sepAbove: Boolean) : ActionNode {

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
        val popup = object : ListPopupImpl(ActionStep(component, actions)) {
            override fun getListElementRenderer(): ListCellRenderer<*> {
                return ActionRenderer(this)
            }
        }
        popup.registerAction(ACTION_EDITOR_ESCAPE) { cancel() }
        popup.registerAction(ACTION_EDITOR_MOVE_CARET_DOWN) { select(list, 1) }
        popup.registerAction(ACTION_EDITOR_MOVE_CARET_UP) { select(list, -1) }

        actions.forEach { (action, keys, _) ->
            keys.forEach { key ->
                popup.content.registerKeyboardAction({ e ->
                    popup.closeOk(null)
                    action.performAction(component, e.modifiers)
                }, key, JComponent.WHEN_IN_FOCUSED_WINDOW)
            }
        }

        popup.showInBestPositionFor(e.dataContext)
    }

    private fun select(list: JList<Any>, increment: Int) {
        val i = list.selectedIndex + increment
        list.selectedIndex = (i + list.model.size) % list.model.size
    }
}

private fun ListPopupImpl.registerAction(actionId: String, run: ListPopupImpl.() -> Unit) {
    KeymapManager.getInstance().activeKeymap
            .getShortcuts(actionId)
            .filterIsInstance<KeyboardShortcut>()
            .filter { it.secondKeyStroke == null }
            .map { it.firstKeyStroke }
            .forEach { key ->
                content.registerKeyboardAction({
                    run()
                }, key, WHEN_IN_FOCUSED_WINDOW)
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
