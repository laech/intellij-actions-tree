package com.gitlab.lae.intellij.actions.tree

import com.google.gson.Gson
import com.google.gson.JsonElement
import java.io.Reader
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.KeyStroke

private val gson = Gson()

fun parseJsonActions(path: Path): List<ActionNode> =
        Files.newBufferedReader(path).use(::parseJsonActions)

fun parseJsonActions(reader: Reader) =
        gson.fromJson(reader, JsonElement::class.java).toActionNode(0).items

private fun JsonElement.toActionNode(count: Int): ActionNode {
    val o = asJsonObject
    val id = o.remove("id")?.asString ?: "ActionNode$count"
    val sep = o.remove("separator-above")?.asBoolean ?: false
    val name = o.remove("name")?.asString
    val keys = o.remove("keys")?.asJsonArray
            ?.map { it.toKeyStroke() }
            ?: emptyList()
    val items = o.remove("items")?.asJsonArray
            ?.map { it.toActionNode(count + 1) }
            ?: emptyList()

    if (!o.keySet().isEmpty()) {
        throw IllegalArgumentException("Invalid elements: ${o.keySet()}")
    }
    return ActionNode(id, name, sep, keys, items)
}

private fun JsonElement.toKeyStroke() = KeyStroke.getKeyStroke(asString)
        ?: throw IllegalArgumentException("Invalid key stroke: $asString")
