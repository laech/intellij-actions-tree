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
    val keys = node.getAsJsonArray("keys").map(::readKeyStroke)
    val id = node.getAsJsonPrimitive("id").asString
    val sep = node.getAsJsonPrimitive("sep")?.asBoolean ?: false
    return ActionRef(keys, id, sep)
}

private fun readActionGroup(node: JsonObject): ActionGroup {
    val keys = node.getAsJsonArray("keys").map(::readKeyStroke)
    val items = node.getAsJsonArray("items")
    return ActionGroup(keys, items.map { readActionNode(it.asJsonObject) })
}

private fun readKeyStroke(node: JsonElement): KeyStroke {
    return KeyStroke.getKeyStroke(node.asString)
            ?: throw IllegalArgumentException("Invalid key: $node")
}
