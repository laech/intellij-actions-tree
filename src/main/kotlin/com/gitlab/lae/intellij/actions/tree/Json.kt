package com.gitlab.lae.intellij.actions.tree

import com.google.gson.*
import java.io.Reader
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.KeyStroke

private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(
                ActionNode::class.java,
                JsonDeserializer { e, _, _ ->
                    readActionNode(e.asJsonObject)
                }
        )
        .create()

fun parseJsonActions(path: Path): List<ActionNode> =
        Files.newBufferedReader(path).use(::parseJsonActions)

fun parseJsonActions(reader: Reader): List<ActionNode> =
        gson.fromJson(reader, ActionGroup::class.java).items

private fun readActionNode(node: JsonObject): ActionNode {
    return if (node.has("items")) {
        readActionGroup(node)
    } else {
        readActionRef(node)
    }
}

private fun readActionRef(node: JsonObject): ActionRef {
    val key = readKeyStroke(node.getAsJsonPrimitive("key"))
    val id = node.getAsJsonPrimitive("id").asString
    return ActionRef(key, id)
}

private fun readActionGroup(node: JsonObject): ActionGroup {
    val key = readKeyStroke(node.getAsJsonPrimitive("key"))
    val items = node.getAsJsonArray("items")
    return ActionGroup(key, items.map { readActionNode(it.asJsonObject) })
}

private fun readKeyStroke(node: JsonPrimitive?): KeyStroke? {
    node ?: return null
    return KeyStroke.getKeyStroke(node.asString)
            ?: throw IllegalArgumentException("Invalid key: $node")
}
