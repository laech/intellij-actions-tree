package com.gitlab.lae.intellij.actions.tree

import com.gitlab.lae.intellij.actions.tree.When.Never
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock

class ActionNodeTest {

  @Test
  fun `prepares child item for context`() {

    val a = actionNode("a", keys = keys("typed a"))
    val b = actionNode("b", keys = keys("typed b", "typed z"))
    val c = actionNode("c", keys = keys("typed b"))
    val d = actionNode("d", keys = keys("typed b"), condition = Never)

    val actual = actionNode(items = listOf(a, b, c, d)).prepare(mock())
    val expected = listOf(
      keys("typed a") to a,
      keys("typed z") to b,
      keys("typed b") to c,
    )
    assertEquals(expected, actual)
  }

  @Test
  fun `prepares child item for context keeps empty key strokes`() {

    val a = actionNode("a")
    val b = actionNode("b", keys = keys("typed b"))
    val c = actionNode("c")

    val actual = actionNode(items = listOf(a, b, c)).prepare(mock())
    val expected = listOf(
      keys() to a,
      keys("typed b") to b,
      keys() to c,
    )
    assertEquals(expected, actual)
  }
}
