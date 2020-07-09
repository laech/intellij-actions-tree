package com.gitlab.lae.intellij.actions.tree

import com.intellij.openapi.actionSystem.DataContext
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import javax.swing.KeyStroke.getKeyStroke

class ActionNodeTest {

  @Test
  fun `prepares child item for context`() {

    val a = ActionNode(
      "a",
      "a",
      null,
      false,
      When.ALWAYS,
      listOf(getKeyStroke('a')),
      emptyList()
    )

    val b = ActionNode(
      "b",
      "b",
      null,
      false,
      When.ALWAYS,
      listOf(
        getKeyStroke('b'),
        getKeyStroke('z')
      ),
      emptyList()
    )

    val c = ActionNode(
      "c",
      "c",
      null,
      false,
      When.ALWAYS,
      listOf(getKeyStroke('b')),
      emptyList()
    )

    val d = ActionNode(
      "d",
      "d",
      null,
      false,
      When.NEVER,
      listOf(getKeyStroke('b')),
      emptyList()
    )

    val actual = ActionNode(
      "id",
      "name",
      null,
      false,
      When.ALWAYS,
      emptyList(),
      listOf(a, b, c, d)
    ).prepare(mock(DataContext::class.java))

    val expected = listOf(
      listOf(getKeyStroke('a')) to a,
      listOf(getKeyStroke('z')) to b,
      listOf(getKeyStroke('b')) to c
    )

    assertEquals(expected, actual)
  }

  @Test
  fun `prepares child item for context keeps empty key strokes`() {

    val a = ActionNode(
      "a",
      "a",
      null,
      false,
      When.ALWAYS,
      emptyList(),
      emptyList()
    )

    val b = ActionNode(
      "b",
      "b",
      null,
      false,
      When.ALWAYS,
      listOf(getKeyStroke('b')),
      emptyList()
    )

    val c = ActionNode(
      "c",
      "c",
      null,
      false,
      When.ALWAYS,
      emptyList(),
      emptyList()
    )

    val actual = ActionNode(
      "id",
      "name",
      null,
      false,
      When.ALWAYS,
      emptyList(),
      listOf(a, b, c)
    ).prepare(mock(DataContext::class.java))

    val expected = listOf(
      emptyList<Any>() to a,
      listOf(getKeyStroke('b')) to b,
      emptyList<Any>() to c
    )

    assertEquals(expected, actual)
  }
}
