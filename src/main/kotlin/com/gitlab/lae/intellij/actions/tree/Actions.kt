package com.gitlab.lae.intellij.actions.tree

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_ESCAPE
import com.intellij.openapi.actionSystem.PlatformDataKeys.CONTEXT_COMPONENT
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.keymap.KeymapManager
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.AsyncResult
import com.intellij.util.Consumer
import java.awt.Component
import java.awt.event.ActionEvent
import java.io.Reader
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.AbstractAction
import javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW
import javax.swing.KeyStroke

sealed class ActionNode {
    abstract val keys: List<KeyStroke>
}

data class ActionRef @JsonCreator constructor(
        @JsonProperty("keys") private val _keys: List<KeyStroke>?,
        @JsonProperty("id", required = true) val id: String
) : ActionNode() {

    override val keys get() = _keys ?: emptyList()
}

data class ActionContainer @JsonCreator constructor(
        @JsonProperty("keys") private val _keys: List<KeyStroke>?,
        @JsonProperty("name") private val _name: String?,
        @JsonProperty("items", required = true) val items: List<ActionNode>
) : ActionNode() {

    override val keys get() = _keys ?: emptyList()

    val name get() = _name ?: "..."
}

data class ActionSeparator @JsonCreator constructor(
        @JsonProperty("separator") val name: String?
) : ActionNode() {

    override val keys: List<KeyStroke> get() = emptyList()
}

private val mapper = ObjectMapper().registerModule(SimpleModule()
        .addDeserializer(KeyStroke::class.java, deserializer(::readKeyStroke))
        .addDeserializer(ActionNode::class.java, deserializer(::readActionNode)))

fun parseJsonActions(path: Path): List<ActionNode> =
        Files.newBufferedReader(path).use(::parseJsonActions)

fun parseJsonActions(reader: Reader) =
        mapper.readValue(reader, ActionContainer::class.java).items

private inline fun <reified T> deserializer(crossinline f: (JsonParser) -> T) =
        object : StdDeserializer<T>(T::class.java) {
            override fun deserialize(p: JsonParser, ctx: DeserializationContext) = f(p)
        }

private fun readKeyStroke(p: JsonParser) =
        KeyStroke.getKeyStroke(p.text)
                ?: throw IllegalArgumentException(
                        "Invalid key stroke: ${p.text}")

private fun readActionNode(p: JsonParser): ActionNode = p.codec.readTree<JsonNode>(p).run {
    when {
        has("id") -> mapper.treeToValue<ActionRef>(this, ActionRef::class.java)
        has("separator") -> mapper.treeToValue<ActionSeparator>(this, ActionSeparator::class.java)
        else -> mapper.treeToValue<ActionContainer>(this, ActionContainer::class.java)
    }
}

fun ActionNode.toAction(mgr: ActionManager): AnAction? = when (this) {
    is ActionContainer -> ActionGroupWrapper(keys, toActionGroup(mgr))
    is ActionSeparator -> Separator(name)
    is ActionRef -> mgr.getAction(id)?.let {
        return object : ActionWrapper(keys, it) {
            override fun actionPerformed(e: AnActionEvent) {
                if (!showPopupIfGroup(e)) super.actionPerformed(e)
            }
        }
    }
}

private fun ActionContainer.toActionGroup(mgr: ActionManager) = object : ActionGroup(name, true) {
    override fun canBePerformed(context: DataContext) = true
    override fun actionPerformed(e: AnActionEvent) = showPopup(e)
    override fun isDumbAware() = true
    override fun getChildren(e: AnActionEvent?) =
            items.mapNotNull { it.toAction(mgr) }.toTypedArray()
}

private fun ActionContainer.showPopup(e: AnActionEvent) {
    val component = e.dataContext.getData(CONTEXT_COMPONENT)
    val mgr = e.actionManager
    val factory = JBPopupFactory.getInstance()
    val step = ActionStep(factory.createActionsStep(
            toActionGroup(e.actionManager),
            e.dataContext, false, true, null, component, false, -1, false))

    val popup = factory.createListPopup(step)

    items.forEach { item ->
        item.keys.forEach { key ->
            val actionMap = popup.content.actionMap
            val inputMap = popup.content.getInputMap(WHEN_IN_FOCUSED_WINDOW)
            inputMap.put(key, item)
            actionMap.put(item, object : AbstractAction() {
                override fun actionPerformed(e: ActionEvent) {
                    popup.setFinalRunnable {
                        item.toAction(mgr)?.performAction(component, e.modifiers)
                    }
                    popup.closeOk(null)
                }
            })
        }
    }

    KeymapManager.getInstance().activeKeymap
            .getShortcuts(ACTION_EDITOR_ESCAPE)
            .filterIsInstance<KeyboardShortcut>()
            .filter { it.secondKeyStroke == null }
            .map { it.firstKeyStroke }
            .forEach { key ->
                val inputMap = popup.content.getInputMap(WHEN_IN_FOCUSED_WINDOW)
                val actionMap = popup.content.actionMap
                val actionKey = "cancel"
                inputMap.put(key, actionKey)
                actionMap.put(actionKey, object : AbstractAction() {
                    override fun actionPerformed(e: ActionEvent) {
                        popup.cancel()
                    }
                })
            }

    popup.showInBestPositionFor(e.dataContext)
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

    val action = (this as? ActionWrapper)?.action ?: this
    if (action.showPopupIfGroup(event)) return
    if (ActionUtil.lastUpdateAndCheckDumb(this, event, false)) {
        ActionUtil.performActionDumbAware(this, event)
    }
}

private fun AnAction.showPopupIfGroup(e: AnActionEvent): Boolean {
    if (this !is ActionGroup || this.canBePerformed(e.dataContext)) {
        return false
    }
    JBPopupFactory.getInstance()
            .createActionGroupPopup(
                    e.presentation.text, this,
                    e.dataContext, true, true, true, null, 30, null)
            .showInBestPositionFor(e.dataContext)
    return true
}