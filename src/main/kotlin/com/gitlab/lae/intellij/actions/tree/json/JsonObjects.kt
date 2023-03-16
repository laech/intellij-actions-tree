package com.gitlab.lae.intellij.actions.tree.json

import com.google.gson.JsonElement
import com.google.gson.JsonObject

fun JsonObject.removeString(
    field: String,
    defaultValue: () -> String,
): String = remove(field)?.asString ?: defaultValue()

fun JsonObject.removeBoolean(
    field: String,
    defaultValue: () -> Boolean,
): Boolean = remove(field)?.asBoolean ?: defaultValue()

fun <T> JsonObject.removeArray(
    field: String,
    mapper: (JsonElement) -> T,
): List<T> = remove(field)?.asJsonArray?.map(mapper) ?: emptyList()
