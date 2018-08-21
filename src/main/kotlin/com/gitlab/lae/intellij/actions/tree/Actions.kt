package com.gitlab.lae.intellij.actions.tree

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_ESCAPE
import com.intellij.openapi.actionSystem.PlatformDataKeys.CONTEXT_COMPONENT
import com.intellij.openapi.keymap.KeymapManager
import com.intellij.ui.popup.PopupFactoryImpl.*
import com.intellij.ui.popup.PopupFactoryImpl.ActionGroupPopup.getActionItems
import java.awt.event.ActionEvent
import java.io.Reader
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.AbstractAction
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
    is ActionSeparator -> Separator(name)
    is ActionRef -> mgr.getAction(id)?.let {
        if (it is ActionGroup) ActionGroupWrapper(keys, it)
        else ActionWrapper(keys, it)
    }
    is ActionContainer -> ActionWrapper(keys, object : AnAction(name) {
        override fun actionPerformed(e: AnActionEvent) = showPopup(e)
        override fun isDumbAware() = true
    })
}

private fun ActionContainer.toActionGroup(mgr: ActionManager) = object : ActionGroup(name, true) {
    override fun isDumbAware() = true
    override fun getChildren(e: AnActionEvent?) =
            items.mapNotNull { it.toAction(mgr) }.toTypedArray()
}

private fun ActionContainer.showPopup(e: AnActionEvent) {
    val component = e.dataContext.getData(CONTEXT_COMPONENT)
    val place = ActionPlaces.UNKNOWN
    val items = getActionItems(
            toActionGroup(e.actionManager),
            e.dataContext, false, false, true, false, place)

    val step = object : ActionPopupStep(items, null, component, false, null, false, true) {
        override fun isSpeedSearchEnabled() = false
    }

    val popup = object : ActionGroupPopup(null, step, null, e.dataContext, place, 30) {
        override fun getListElementRenderer() = ActionRenderer(this)
    }

    // Removes the default behaviour of jumping to an item when keys are typed,
    popup.list.keyListeners
            .filter { it.javaClass.name == "javax.swing.plaf.basic.BasicListUI\$Handler" }
            .forEach { popup.list.removeKeyListener(it) }

    items.forEachIndexed { i, item ->
        item.keys().forEach { key ->
            popup.registerAction("action-$i", key, object : AbstractAction() {
                override fun actionPerformed(e: ActionEvent) {
                    popup.list.selectedIndex = i
                    popup.handleSelect(true)
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
                popup.registerAction("cancel", key, object : AbstractAction() {
                    override fun actionPerformed(e: ActionEvent) {
                        popup.cancel()
                    }
                })
            }

    popup.showInBestPositionFor(e.dataContext)
}

fun ActionItem.keys() = action.shortcutSet.shortcuts
        .asSequence()
        .filterIsInstance<KeyboardShortcut>()
        .filter { it.secondKeyStroke == null }
        .map { it.firstKeyStroke }
        .toList()
