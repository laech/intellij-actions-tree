package com.gitlab.lae.intellij.actions.tree

import com.intellij.openapi.actionSystem.CommonDataKeys.PROJECT
import com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.actionSystem.PlatformDataKeys.EDITOR
import com.intellij.openapi.actionSystem.PlatformDataKeys.TOOL_WINDOW
import com.intellij.openapi.wm.IdeFocusManager
import java.nio.file.Files.exists
import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.Predicate
import java.util.regex.Pattern
import javax.swing.text.JTextComponent

interface When : Predicate<DataContext> {

  data class Any(val clauses: List<When>) : When {

    constructor(vararg clauses: When) :
      this(listOf(*clauses))

    override fun test(context: DataContext) =
      clauses.any { it.test(context) }
  }

  data class All(val clauses: List<When>) : When {

    constructor(vararg clauses: When) :
      this(listOf(*clauses))

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

    constructor(regex: String) :
      this(EqPattern(regex))

    override fun value(context: DataContext): String? {
      val window = context.getData(TOOL_WINDOW)
      return if (window != null && window.isActive) window.stripeTitle else null
    }
  }

  data class ToolWindowTabActive(override val regex: EqPattern) : Regex {

    constructor(regex: String) :
      this(EqPattern(regex))

    override fun value(context: DataContext): String? {
      val window = context.getData(TOOL_WINDOW)
      return if (window != null && window.isActive) window.title else null
    }
  }

  data class FileExtension(override val regex: EqPattern) : Regex {

    constructor(regex: String) :
      this(EqPattern(regex))

    override fun value(context: DataContext) =
      context.getData(VIRTUAL_FILE)?.extension
  }

  data class PathExists(val path: Path) : When {

    constructor(path: String) :
      this(Paths.get(path))

    override fun test(context: DataContext) =
      if (path.isAbsolute) {
        exists(path)
      } else {
        context.getData(PROJECT)?.basePath?.let {
          exists(Paths.get(it).resolve(path))
        } ?: false
      }
  }

  object Always : When {
    override fun toString() = "When.Always"
    override fun test(context: DataContext) = true
  }

  object Never : When {
    override fun toString() = "When.Never"
    override fun test(context: DataContext) = false
  }

  object InputFocused : When {
    override fun toString() = "When.InputFocused"
    override fun test(context: DataContext): Boolean {
      val focusManager = IdeFocusManager.findInstanceByContext(context)
      val component = focusManager.focusOwner
      return component is JTextComponent && component.isEditable
    }
  }

  object TextSelected : When {
    override fun toString() = "When.TextSelected"

    override fun test(context: DataContext): Boolean {
      val focusManager = IdeFocusManager.findInstanceByContext(context)
      val component = focusManager.focusOwner
      if (component !is JTextComponent) {
        return false
      }

      val hasComponentSelection =
        component.selectionStart < component.selectionEnd

      val hasEditorSelection =
        component is DataProvider
          && EDITOR.getData(component)?.selectionModel?.hasSelection(true)
          ?: false

      return hasComponentSelection || hasEditorSelection;
    }
  }

  companion object {

    fun parse(input: String): When =
      if (input.startsWith("!")) Not(doParse(input.substring(1)))
      else doParse(input)

    private fun doParse(input: String): When {
      when (input) {
        "InputFocused" -> return InputFocused
        "TextSelected" -> return TextSelected
      }

      val parts = input.split(":".toRegex(), 2).toTypedArray()
      require(parts.size == 2) { "Invalid 'when' pattern: '$input'" }

      val type = parts[0]
      val arg = parts[1]
      return when (type) {
        "ToolWindowActive" -> ToolWindowActive(arg)
        "ToolWindowTabActive" -> ToolWindowTabActive(arg)
        "FileExtension" -> FileExtension(arg)
        "PathExists" -> PathExists(arg)
        else -> throw IllegalArgumentException(input)
      }
    }
  }
}
