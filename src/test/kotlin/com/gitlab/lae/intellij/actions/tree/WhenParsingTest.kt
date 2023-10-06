package com.gitlab.lae.intellij.actions.tree

import com.gitlab.lae.intellij.actions.tree.When.FileExtension
import com.gitlab.lae.intellij.actions.tree.When.InputFocused
import com.gitlab.lae.intellij.actions.tree.When.Not
import com.gitlab.lae.intellij.actions.tree.When.PathExists
import com.gitlab.lae.intellij.actions.tree.When.TextSelected
import com.gitlab.lae.intellij.actions.tree.When.ToolWindowActive
import com.gitlab.lae.intellij.actions.tree.When.ToolWindowTabActive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class WhenParsingTest {

  @ParameterizedTest
  @MethodSource("parameters")
  fun `parses input correctly`(input: String, expected: When) {
    assertEquals(expected, When.parse(input))
  }

  companion object {
    @JvmStatic
    fun parameters() =
        arrayOf(
            arrayOf("ToolWindowActive:Project", ToolWindowActive("Project")),
            arrayOf("ToolWindowTabActive:Log", ToolWindowTabActive("Log")),
            arrayOf("FileExtension:rs", FileExtension("rs")),
            arrayOf("!FileExtension:rs", Not(FileExtension("rs"))),
            arrayOf("PathExists:readme", PathExists("readme")),
            arrayOf("InputFocused", InputFocused),
            arrayOf("TextSelected", TextSelected),
        )
  }
}
