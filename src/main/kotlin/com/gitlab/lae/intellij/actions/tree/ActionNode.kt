package com.gitlab.lae.intellij.actions.tree

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_ESCAPE
import com.intellij.openapi.actionSystem.PlatformDataKeys.CONTEXT_COMPONENT
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.keymap.KeymapManager
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.AsyncResult
import com.intellij.ui.components.JBList
import com.intellij.util.Consumer
import java.awt.Component
import javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW
import javax.swing.JList
import javax.swing.KeyStroke

data class ActionNode(
        val id: String,
        val name: String?,
        val separatorAbove: Boolean,
        val keys: List<KeyStroke>,
        val items: List<ActionNode>)

private fun ActionNode.toPresentation(e: AnActionEvent): ActionPresentation? {
    val action = toAction(e.actionManager) ?: return null
    val presentation = action.templatePresentation.clone()
    val event = AnActionEvent(
            null,
            e.dataContext,
            ActionPlaces.UNKNOWN,
            presentation,
            e.actionManager,
            e.modifiers
    )
    event.setInjectedContext(action.isInInjectedContext)

    ActionUtil.performDumbAwareUpdate(true, action, event, false)
    return ActionPresentation(presentation, keys, separatorAbove, action)
}

fun ActionNode.toAction(mgr: ActionManager): AnAction? {
    if (items.isEmpty()) {
        return mgr.getAction(id)
    }
    return object : AnAction(name) {
        override fun isDumbAware() = true
        override fun actionPerformed(e: AnActionEvent) = showPopup(e)
    }
}

private fun ActionNode.showPopup(e: AnActionEvent) {
    val component = e.getData(CONTEXT_COMPONENT)
    val presentations = items.mapNotNull { it.toPresentation(e) }

    val list = JBList<ActionPresentation>(presentations)
    list.cellRenderer = ActionPresentationRenderer()
    list.actionMap.clear()
    list.actionMap.parent = null

    val popup = JBPopupFactory.getInstance()
            .createListPopupBuilder(list)
            .setModalContext(true)
            .setItemChoosenCallback {
                val value = list.selectedValue
                if (value != null && value.presentation.isEnabled) {
                    value.action.performAction(component, 0)
                }
            }
            .createPopup()

    popup.registerKeys(list, component)
    popup.registerAdditionalEscapeKeys(list)
    popup.showInBestPositionFor(e.dataContext)
}

private fun JBPopup.registerKeys(list: JList<ActionPresentation>, comp: Component?) =
        (0 until list.model.size).map(list.model::getElementAt).forEachIndexed { i, item ->
            item.keys.forEach { key ->
                list.registerKeyboardAction({ e ->
                    list.selectedIndex = i
                    if (item.presentation.isEnabled) {
                        setFinalRunnable { item.action.performAction(comp, e.modifiers) }
                        closeOk(null)
                    }
                }, key, WHEN_IN_FOCUSED_WINDOW)
            }
        }

private fun JBPopup.registerAdditionalEscapeKeys(list: JBList<ActionPresentation>) = KeymapManager
        .getInstance().activeKeymap.getShortcuts(ACTION_EDITOR_ESCAPE)
        .filterIsInstance<KeyboardShortcut>()
        .filter { it.secondKeyStroke == null }
        .map { it.firstKeyStroke }
        .forEach { key ->
            list.registerKeyboardAction({ cancel() }, key, WHEN_IN_FOCUSED_WINDOW)
        }

fun AnAction.performAction(component: Component?, modifiers: Int) {
    val dataManager = DataManager.getInstance()
    when (component) {
        null -> dataManager.dataContextFromFocus
        else -> AsyncResult.done(dataManager.getDataContext(component))
    }.doWhenDone(Consumer { dataContext ->
        performAction(dataContext, modifiers)
    })
}

private fun AnAction.performAction(dataContext: DataContext, modifiers: Int) {
    val actions = ActionManager.getInstance()
    val presentation = templatePresentation.clone()
    val place = ActionPlaces.UNKNOWN
    val event = AnActionEvent(null, dataContext, place, presentation, actions, modifiers)
    event.setInjectedContext(isInInjectedContext)

    if (showPopupIfGroup(event)) return
    if (ActionUtil.lastUpdateAndCheckDumb(this, event, false)) {
        ActionUtil.performActionDumbAware(this, event)
    }
}

private fun AnAction.showPopupIfGroup(e: AnActionEvent): Boolean {
    if (this !is ActionGroup || canBePerformed(e.dataContext)) {
        return false
    }
    JBPopupFactory.getInstance()
            .createActionGroupPopup(
                    e.presentation.text, this,
                    e.dataContext, true, true, true, null, 30, null)
            .showInBestPositionFor(e.dataContext)
    return true
}
