package com.gitlab.lae.intellij.actions.tree

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.PlatformDataKeys.CONTEXT_COMPONENT
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.AsyncResult
import com.intellij.util.Consumer
import java.awt.Component
import javax.swing.JComponent
import javax.swing.KeyStroke

sealed class ActionTree(val key: KeyStroke?) : AnAction(), ShortcutProvider {

    init {
        if (key != null) shortcutSet = CustomShortcutSet(key)
    }

    override fun getShortcut() = shortcutSet
}

class ActionLeaf(key: KeyStroke?, id: String) : ActionTree(key) {

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

class ActionNode(
        key: KeyStroke?,
        name: String,
        val items: List<ActionTree>
) : ActionTree(key) {

    init {
        templatePresentation.text = name
    }

    override fun actionPerformed(event: AnActionEvent) {
        val component = event.dataContext.getData(CONTEXT_COMPONENT)
        val popup = JBPopupFactory.getInstance()
                .createListPopup(ActionStep(component, this@ActionNode))

        items.forEach {
            if (it.key != null) {
                popup.content.registerKeyboardAction({ e ->
                    it.performAction(component, e.modifiers)
                    popup.dispose()
                }, it.key, JComponent.WHEN_IN_FOCUSED_WINDOW)
            }
        }

        popup.showInBestPositionFor(event.dataContext)
    }

    override fun isDumbAware() = true

}

fun AnAction.performAction(component: Component?, modifiers: Int) {
    val dataManager = DataManager.getInstance()
    when (component) {
        null -> dataManager.dataContextFromFocus
        else -> AsyncResult.done(dataManager.getDataContext(component))
    }.doWhenDone(Consumer {
        val actions = ActionManager.getInstance()
        val template = templatePresentation.clone()
        val place = ActionPlaces.UNKNOWN
        val event = AnActionEvent(null, it, place, template, actions, modifiers)
        event.setInjectedContext(isInInjectedContext)
        if (ActionUtil.lastUpdateAndCheckDumb(this, event, false)) {
            ActionUtil.performActionDumbAware(this, event)
        }
    })
}
