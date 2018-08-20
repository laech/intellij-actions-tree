package com.gitlab.lae.intellij.actions.tree

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.io.Reader
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.KeyStroke
import javax.swing.KeyStroke.getKeyStroke

private interface JsonAction {
    fun toActionNode(): ActionNode
}

private data class JsonActionRef @JsonCreator constructor(
        @JsonProperty("id", required = true) val id: String,
        @JsonProperty("keys") val keys: List<KeyStroke>?,
        @JsonProperty("header") val header: String?) : JsonAction {

    override fun toActionNode() =
            ActionRef(keys ?: emptyList(), id, header)
}

private data class JsonActionGroup @JsonCreator constructor(
        @JsonProperty("items", required = true) val items: List<JsonAction>,
        @JsonProperty("keys") val keys: List<KeyStroke>?) : JsonAction {

    override fun toActionNode() =
            ActionGroup(keys ?: emptyList(), items.map { it.toActionNode() })
}

private val mapper = ObjectMapper().registerModule(SimpleModule()
        .addDeserializer(KeyStroke::class.java, deserializer(::readKeyStroke))
        .addDeserializer(JsonAction::class.java, deserializer(::readJsonAction)))

fun parseJsonActions(path: Path): List<ActionNode> =
        Files.newBufferedReader(path).use(::parseJsonActions)

fun parseJsonActions(reader: Reader) =
        mapper.readValue(reader, JsonActionGroup::class.java)
                .toActionNode().items

private inline fun <reified T> deserializer(crossinline f: (JsonParser) -> T) =
        object : StdDeserializer<T>(T::class.java) {
            override fun deserialize(p: JsonParser, ctx: DeserializationContext) = f(p)
        }

private fun readKeyStroke(p: JsonParser) =
        getKeyStroke(p.text)
                ?: throw IllegalArgumentException(
                        "Invalid key stroke: ${p.text}")

private fun readJsonAction(p: JsonParser): JsonAction = p.codec.readTree<JsonNode>(p).let {
    if (it.has("id")) {
        mapper.treeToValue<JsonActionRef>(it, JsonActionRef::class.java)
    } else {
        mapper.treeToValue<JsonActionGroup>(it, JsonActionGroup::class.java)
    }
}
