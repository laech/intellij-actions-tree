package com.gitlab.lae.intellij.actions.tree

import com.gitlab.lae.intellij.actions.tree.When.Companion.NEVER
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock

class ActionNodeTest {

  @Test
  fun `prepares child item for context`() {

    val a = action("a", keys = keys("typed a"))
    val b = action("b", keys = keys("typed b", "typed z"))
    val c = action("c", keys = keys("typed b"))
    val d = action("d", keys = keys("typed b"), condition = NEVER)

    val actual = action(items = listOf(a, b, c, d)).prepare(mock())
    val expected = listOf(
      keys("typed a") to a,
      keys("typed z") to b,
      keys("typed b") to c
    )
    assertEquals(expected, actual)
  }

  @Test
  fun `prepares child item for context keeps empty key strokes`() {

    val a = action("a")
    val b = action("b", keys = keys("typed b"))
    val c = action("c")

    val actual = action(items = listOf(a, b, c)).prepare(mock())
    val expected = listOf(
      keys() to a,
      keys("typed b") to b,
      keys() to c
    )
    assertEquals(expected, actual)
  }
}
