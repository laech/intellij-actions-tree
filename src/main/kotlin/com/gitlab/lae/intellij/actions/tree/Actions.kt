package com.gitlab.lae.intellij.actions.tree

import com.google.gson.*
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
import java.lang.reflect.Type
import javax.swing.JComponent
import javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW
import javax.swing.KeyStroke

sealed class ActionTree(key: KeyStroke?) : AnAction(), ShortcutProvider {

    init {
        if (key != null) shortcutSet = CustomShortcutSet(key)
    }

    override fun getShortcut() = shortcutSet
}

data class ActionLeaf(val key: KeyStroke?, val id: String) : ActionTree(key) {

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

data class ActionNode(
        val key: KeyStroke?,
        val name: String?,
        val items: List<AnAction>
) : ActionTree(key) {

    init {
        templatePresentation.text = name
    }

    override fun actionPerformed(event: AnActionEvent) {
        val component = event.dataContext.getData(CONTEXT_COMPONENT)
        val popup = ListPopupImpl(ActionStep(component, this@ActionNode))

        popup.registerAction(ACTION_EDITOR_ESCAPE) { dispose() }
        popup.registerAction(ACTION_EDITOR_MOVE_CARET_DOWN) { list.selectedIndex += 1 }
        popup.registerAction(ACTION_EDITOR_MOVE_CARET_UP) { list.selectedIndex -= 1 }

        items.forEach { action ->
            action.shortcutSet.shortcuts
                    .filterIsInstance<KeyboardShortcut>()
                    .filter { it.secondKeyStroke == null }
                    .map { it.firstKeyStroke }
                    .forEach { key ->
                        popup.content.registerKeyboardAction({ e ->
                            action.performAction(component, e.modifiers)
                            popup.dispose()
                        }, key, JComponent.WHEN_IN_FOCUSED_WINDOW)
                    }
        }

        popup.showInBestPositionFor(event.dataContext)
    }

    override fun isDumbAware() = true

}

private fun ListPopupImpl.registerAction(actionId: String, run: ListPopupImpl.() -> Unit) {
    KeymapManager.getInstance().activeKeymap
            .getShortcuts(actionId)
            .filterIsInstance<KeyboardShortcut>()
            .filter { it.secondKeyStroke == null }
            .map { it.firstKeyStroke }
            .forEach { key ->
                content.registerKeyboardAction({ run() }, key, WHEN_IN_FOCUSED_WINDOW)
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
        val place = ActionPlaces.UNKNOWN
        val event = AnActionEvent(null, it, place, template, actions, modifiers)
        event.setInjectedContext(isInInjectedContext)
        if (ActionUtil.lastUpdateAndCheckDumb(this, event, false)) {
            ActionUtil.performActionDumbAware(this, event)
        }
    })
}

val gson: Gson = GsonBuilder()
        .registerTypeAdapter(AnAction::class.java, ActionTreeDeserializer())
        .registerTypeAdapter(ActionTree::class.java, ActionTreeDeserializer())
        .registerTypeAdapter(KeyStroke::class.java, KeyStrokeDeserializer())
        .create()

private class ActionTreeDeserializer : JsonDeserializer<ActionTree> {

    override fun deserialize(
            node: JsonElement,
            type: Type,
            ctx: JsonDeserializationContext
    ): ActionTree {

        return if (node is JsonObject && node.has("items")) {
            ctx.deserialize(node, ActionNode::class.java)
        } else {
            ctx.deserialize(node, ActionLeaf::class.java)
        }
    }
}

private class KeyStrokeDeserializer : JsonDeserializer<KeyStroke> {

    override fun deserialize(
            node: JsonElement,
            type: Type,
            ctx: JsonDeserializationContext
    ): KeyStroke {
        val str = ctx.deserialize<String>(node, String::class.java)
        return KeyStroke.getKeyStroke(str)
                ?: throw IllegalArgumentException("Invalid key: $str")
    }

}