package com.gitlab.lae.intellij.actions.tree

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class WhenParsingTest(
  private val input: String,
  private val expected: When
) {

  @Test
  fun `parses input correctly`() {
    assertEquals(expected, When.parse(input))
  }

  companion object {
    @JvmStatic
    @Parameters(name = "{0}")
    fun parameters() = arrayOf(
      arrayOf("ToolWindowActive:Project", When.toolWindowActive("Project")),
      arrayOf("ToolWindowTabActive:Log", When.toolWindowTabActive("Log")),
      arrayOf("FileExtension:rs", When.fileExtension("rs")),
      arrayOf("!FileExtension:rs", When.not(When.fileExtension("rs")))
    )
  }
}
