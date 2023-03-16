package com.gitlab.lae.intellij.actions.tree

import com.gitlab.lae.intellij.actions.tree.When.*
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class WhenParsingTest(
  private val input: String,
  private val expected: When,
) {

  @Test
  fun `parses input correctly`() {
    assertEquals(expected, When.parse(input))
  }

  companion object {
    @JvmStatic
    @Parameters(name = "{0}")
    fun parameters() = arrayOf(
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
