package com.gitlab.lae.intellij.actions.tree.json

import com.gitlab.lae.intellij.actions.tree.ActionNode
import com.gitlab.lae.intellij.actions.tree.When
import com.gitlab.lae.intellij.actions.tree.When.All
import com.gitlab.lae.intellij.actions.tree.When.Any
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import java.io.Reader
import java.nio.file.Files.newBufferedReader
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.KeyStroke
import javax.swing.KeyStroke.getKeyStroke

private const val WHEN = "when"
private const val WHEN_ANY = "any"
private const val WHEN_ALL = "all"
private val gson = Gson()

fun parseJsonActions(path: Path): List<ActionNode> = newBufferedReader(path).use(::parseJsonActions)

fun parseJsonActions(reader: Reader): List<ActionNode> {
  val element = gson.fromJson(reader, JsonElement::class.java)
  val seq = AtomicInteger()
  val (_, _, _, _, _, _, items) = toActionNode(element, seq::getAndIncrement)
  return items
}

private fun toActionNode(element: JsonElement, seq: () -> Int): ActionNode {
  val o = element.asJsonObject
  val id = o.removeString("id") { "ActionsTree.Node." + seq() }
  val sep = o.remove("separator-above")?.asString
  val name = o.removeString("name") { "Unnamed" }
  val sticky = o.removeBoolean("sticky") { false }
  val condition = processWhen(o.remove(WHEN))
  val keys = o.removeArray("keys", ::toKeyStroke)
  val items = o.removeArray("items") { toActionNode(it, seq) }
  require(o.keySet().isEmpty()) { "Invalid elements: " + o.keySet() }
  return ActionNode(id, name, sep, sticky, condition, keys, items)
}

private fun processWhen(element: JsonElement?): When {
  element ?: return When.Always

  if (element.isJsonPrimitive) {
    return When.parse(element.asString)
  }

  val obj = element.asJsonObject
  require(obj.keySet().isNotEmpty()) { "'$WHEN' object is empty" }
  require(obj.keySet().size == 1) {
    "'$WHEN' object must only have either '$WHEN_ANY' or '$WHEN_ALL' element: $obj"
  }

  val any = obj.remove(WHEN_ANY)
  if (any != null) {
    return Any(*processWhens(any.asJsonArray))
  }

  val all = obj.remove(WHEN_ALL)
  if (all != null) {
    return All(*processWhens(all.asJsonArray))
  }

  throw IllegalArgumentException(
      "'$WHEN' object must only have either '$WHEN_ANY' or '$WHEN_ALL' element: $obj",
  )
}

private fun processWhens(clauses: JsonArray): Array<When> =
    clauses.asJsonArray.map(::processWhen).toTypedArray()

private fun toKeyStroke(element: JsonElement): KeyStroke =
    getKeyStroke(element.asString)
        ?: throw IllegalArgumentException("Invalid key stroke: " + element.asString)
