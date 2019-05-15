package com.gitlab.lae.intellij.actions.tree

import com.intellij.ide.DataManager
import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_ESCAPE
import com.intellij.openapi.actionSystem.PlatformDataKeys.CONTEXT_COMPONENT
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.JBPopupListener
import com.intellij.openapi.ui.popup.LightweightWindowEvent
import com.intellij.openapi.util.AsyncResult
import com.intellij.ui.components.JBList
import com.intellij.util.Consumer
import java.awt.Component
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.KeyStroke

data class ActionNode(
        val id: String,
        val name: String?,
        val separatorAbove: String?,
        val sticky: Boolean,
        val keys: List<KeyStroke>,
        val items: List<ActionNode>)

// Use ACTION_SEARCH as the action place seems to work the best.
//
// 'Run | Stop' menu action works correctly this way by
// showing a list of processes to stop
//
// 'Exit' actions works (doesn't work if place is MAIN_MENU)
//
private const val actionPlace = ActionPlaces.ACTION_SEARCH

private fun ActionNode.toPresentation(e: AnActionEvent): ActionPresentation {
    val action = toAction(e.actionManager)
    val presentation = action.templatePresentation.clone()
    val event = AnActionEvent(
            null,
            e.dataContext,
            actionPlace,
            presentation,
            e.actionManager,
            e.modifiers
    )
    event.setInjectedContext(action.isInInjectedContext)

    ActionUtil.performDumbAwareUpdate(true, action, event, false)
    return ActionPresentation(presentation, keys, separatorAbove, sticky, action)
}

fun ActionNode.toAction(mgr: ActionManager) = if (items.isEmpty()) {
    mgr.getAction(id) ?: object : AnAction("?$id?") {
        override fun actionPerformed(e: AnActionEvent) = Unit
        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = false
        }
    }
} else {
    object : AnAction(name) {
        override fun isDumbAware() = true
        override fun actionPerformed(e: AnActionEvent) = showPopup(e)
    }
}

private fun ActionNode.showPopup(e: AnActionEvent) {
    val component = e.getData(CONTEXT_COMPONENT)
    val presentations = items.map { it.toPresentation(e) }

    var popup: JBPopup? = null
    val list = ActionList(presentations)
    list.cellRenderer = ActionPresentationRenderer()

    // Register our action first before IntelliJ registers the default
    // actions (e.g. com.intellij.ui.ScrollingUtil) so that in case of
    // conflict our action will be executed
    registerKeys(list, component, e.getData(EDITOR)) { popup }
    registerIdeAction(list, ACTION_EDITOR_ESCAPE, e.actionManager) { popup?.cancel() }

    popup = JBPopupFactory.getInstance()
            .createListPopupBuilder(list)
            .setModalContext(true)
            .setItemChoosenCallback {
                val value = list.selectedValue
                if (value != null && value.presentation.isEnabled) {
                    value.action.performAction(component, 0)
                }
            }
            .createPopup()

    val dispatcher = ActionDispatcher(popup, list)
    popup.addListener(object : JBPopupListener {
        val pm = IdeEventQueue.getInstance().popupManager
        override fun beforeShown(event: LightweightWindowEvent) = pm.push(dispatcher)
        override fun onClosed(event: LightweightWindowEvent) = pm.remove(dispatcher)
    })
    popup.showInBestPositionFor(e.dataContext)
}

private fun registerKeys(
        list: JList<ActionPresentation>,
        component: Component?,
        editor: Editor?,
        getPopup: () -> JBPopup?
) {
    (0 until list.model.size).map(list.model::getElementAt).forEachIndexed { i, item ->
        if (item.keys.isEmpty()) {
            return@forEachIndexed
        }

        val inputMap = list.inputMap
        val actionMap = list.actionMap
        item.keys.forEach { key ->
            inputMap.put(key, key)
            actionMap.put(key, object : AbstractAction() {
                override fun actionPerformed(e: ActionEvent) {
                    if (!item.presentation.isEnabled) return
                    val popup = getPopup() ?: return
                    list.selectedIndex = i

                    val runnable = Runnable {
                        item.action.performAction(component, e.modifiers)
                    }
                    if (!item.sticky) {
                        popup.setFinalRunnable(runnable)
                        popup.closeOk(null)
                        return
                    }

                    runnable.run()
                    if (editor != null) {
                        editor.scrollingModel.runActionOnScrollingFinished {
                            popup.setLocation(JBPopupFactory.getInstance()
                                    .guessBestPopupLocation(editor)
                                    .screenPoint)
                        }
                    } else if (component is JComponent) {
                        popup.setLocation(JBPopupFactory.getInstance()
                                .guessBestPopupLocation(component)
                                .screenPoint)
                    }
                }
            })
        }
    }
}

private fun registerIdeAction(
        list: JBList<ActionPresentation>,
        actionId: String,
        actionManager: ActionManager,
        run: () -> Unit
) {
    val shortcutSet = actionManager.getAction(actionId)?.shortcutSet ?: return
    if (shortcutSet.shortcuts.isEmpty()) return
    object : AnAction() {
        override fun actionPerformed(e: AnActionEvent) {
            run()
        }
    }.registerCustomShortcutSet(shortcutSet, list)
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
    val event = AnActionEvent(null, dataContext, actionPlace, presentation, actions, modifiers)
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
            .createActionGroupPopup(e.presentation.text, this, e.dataContext,
                    JBPopupFactory.ActionSelectionAid.NUMBERING, true)
            .showInBestPositionFor(e.dataContext)
    return true
}
