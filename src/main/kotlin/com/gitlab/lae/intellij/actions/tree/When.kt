package com.gitlab.lae.intellij.actions.tree

import com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys.TOOL_WINDOW
import com.intellij.openapi.wm.IdeFocusManager
import java.util.Collections.unmodifiableList
import java.util.function.Predicate
import java.util.regex.Pattern
import javax.swing.text.JTextComponent

interface When : Predicate<DataContext> {

  data class Any(val clauses: List<When>) : When {
    override fun test(context: DataContext) =
      clauses.any { it.test(context) }
  }

  data class All(val clauses: List<When>) : When {
    override fun test(context: DataContext) =
      clauses.all { it.test(context) }
  }

  data class Not(val condition: When) : When {
    override fun test(context: DataContext) =
      !condition.test(context)
  }

  class EqPattern(val pattern: Pattern) {
    constructor(pattern: String) : this(Pattern.compile(pattern))

    override fun toString() = pattern.toString()
    override fun hashCode() = pattern.hashCode()
    override fun equals(other: kotlin.Any?) =
      other is EqPattern && pattern.pattern() == other.pattern.pattern()
  }

  interface Regex : When {
    val regex: EqPattern
    fun value(context: DataContext): String?
    override fun test(context: DataContext): Boolean {
      val value = value(context) ?: return false
      return regex.pattern.matcher(value).matches()
    }
  }

  data class ToolWindowActive(override val regex: EqPattern) : Regex {
    override fun value(context: DataContext): String? {
      val window = context.getData(TOOL_WINDOW)
      return if (window != null && window.isActive) window.stripeTitle else null
    }
  }

  data class ToolWindowTabActive(override val regex: EqPattern) : Regex {
    override fun value(context: DataContext): String? {
      val window = context.getData(TOOL_WINDOW)
      return if (window != null && window.isActive) window.title else null
    }
  }

  data class FileExtension(override val regex: EqPattern) : Regex {
    override fun value(context: DataContext) =
      context.getData(VIRTUAL_FILE)?.extension
  }

  companion object {

    val ALWAYS: When = object : When {
      override fun test(context: DataContext) = true
      override fun toString() = "When.ALWAYS"
    }

    val NEVER: When = object : When {
      override fun test(context: DataContext) = false
      override fun toString() = "When.NEVER"
    }

    val INPUT_FOCUSED: When = object : When {
      override fun test(context: DataContext): Boolean {
        val focusManager = IdeFocusManager.findInstanceByContext(context)
        val component = focusManager.focusOwner
        return component is JTextComponent && component.isEditable
      }

      override fun toString() = "When.INPUT_FOCUSED"
    }

    fun parse(input: String): When =
      if (input.startsWith("!")) not(doParse(input.substring(1)))
      else doParse(input)

    private fun doParse(input: String): When {
      if (input == "InputFocused") {
        return INPUT_FOCUSED
      }

      val parts = input.split(":".toRegex(), 2).toTypedArray()
      require(parts.size == 2) { "Invalid 'when' pattern: '$input'" }

      val type = parts[0]
      val arg = parts[1]
      return when (type) {
        "ToolWindowActive" -> toolWindowActive(arg)
        "ToolWindowTabActive" -> toolWindowTabActive(arg)
        "FileExtension" -> fileExtension(arg)
        else -> throw IllegalArgumentException(input)
      }
    }

    fun any(vararg clauses: When): When =
      Any(unmodifiableList(listOf(*clauses)))

    fun all(vararg clauses: When): When =
      All(unmodifiableList(listOf(*clauses)))

    fun toolWindowActive(titleRegex: String): When =
      ToolWindowActive(EqPattern(titleRegex))

    fun toolWindowTabActive(tabTitleRegex: String): When =
      ToolWindowTabActive(EqPattern(tabTitleRegex))

    fun fileExtension(extRegex: String): When =
      FileExtension(EqPattern(extRegex))

    fun not(condition: When): When = Not(condition)
  }
}
