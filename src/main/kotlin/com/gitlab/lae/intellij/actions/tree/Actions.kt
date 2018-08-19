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

sealed class ActionNode : AnAction(), ShortcutProvider {

    abstract val keys: List<KeyStroke>

    override fun getShortcut() = CustomShortcutSet(*keys
            .map { KeyboardShortcut(it, null) }
            .toTypedArray())
}

data class ActionRef(
        override val keys: List<KeyStroke>,
        val id: String,
        val sep: Boolean
) : ActionNode() {

    init {
        val action = ActionManager.getInstance().getActionOrStub(id)
        if (action != null) {
            templatePresentation.copyFrom(action.templatePresentation)
        } else {
            templatePresentation.text = id
            templatePresentation.isEnabled = false
        }
    }

    private val action: AnAction? by lazy {
        ActionManager.getInstance().getAction(id)
    }

    override fun actionPerformed(e: AnActionEvent) {
        action?.actionPerformed(e)
    }

    override fun update(e: AnActionEvent) {
        val a = action
        if (a != null) {
            a.update(e)
            templatePresentation.copyFrom(a.templatePresentation)
        }
    }

    override fun isDumbAware() = action?.isDumbAware ?: true

    override fun setInjectedContext(worksInInjected: Boolean) {
        super.setInjectedContext(worksInInjected)
        action?.setInjectedContext(worksInInjected)
    }

    override fun isTransparentUpdate() =
            action?.isTransparentUpdate ?: super.isTransparentUpdate()

    override fun setDefaultIcon(isDefaultIconSet: Boolean) {
        super.setDefaultIcon(isDefaultIconSet)
        action?.isDefaultIcon = isDefaultIcon
    }

    override fun startInTransaction() =
            action?.startInTransaction() ?: super.startInTransaction()

    override fun isInInjectedContext() =
            action?.isInInjectedContext ?: super.isInInjectedContext()

    override fun isDefaultIcon() =
            action?.isDefaultIcon ?: super.isDefaultIcon()
}

data class ActionGroup(
        override val keys: List<KeyStroke>,
        val items: List<ActionNode>
) : ActionNode() {

    override fun actionPerformed(event: AnActionEvent) {
        val component = event.dataContext.getData(CONTEXT_COMPONENT)
        val popup = object : ListPopupImpl(ActionStep(component, this@ActionGroup)) {
            override fun getListElementRenderer(): ListCellRenderer<*> {
                return ActionRenderer(this)
            }
        }
        popup.registerAction(ACTION_EDITOR_ESCAPE) { cancel() }
        popup.registerAction(ACTION_EDITOR_MOVE_CARET_DOWN) { select(list, 1) }
        popup.registerAction(ACTION_EDITOR_MOVE_CARET_UP) { select(list, -1) }

        items.forEach { action ->
            action.keys.forEach { key ->
                popup.content.registerKeyboardAction({ e ->
                    popup.closeOk(null)
                    action.performAction(component, e.modifiers)
                }, key, JComponent.WHEN_IN_FOCUSED_WINDOW)
            }
        }

        popup.showInBestPositionFor(event.dataContext)
    }

    override fun isDumbAware() = true

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
