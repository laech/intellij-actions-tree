package com.gitlab.lae.intellij.actions.tree.json

import com.gitlab.lae.intellij.actions.tree.ActionNode
import com.gitlab.lae.intellij.actions.tree.When
import org.junit.Assert.assertEquals
import org.junit.Test
import javax.swing.KeyStroke.getKeyStroke
import kotlin.text.Charsets.UTF_8

class ActionNodeParserTest {

  @Test
  fun deserialization() {
    val expected = listOf(
      ActionNode(
        "ActionsTree.Node.1",
        "Unnamed",
        null,
        false,
        When.toolWindowActive("Project"),
        listOf(getKeyStroke("ctrl C")),
        listOf(
          ActionNode(
            "ActionsTree.Node.2",
            "Unnamed",
            null,
            false,
            When.ALWAYS,
            listOf(getKeyStroke("P")),
            listOf(
              ActionNode(
                "CloseProject",
                "Unnamed",
                null,
                false,
                When.ALWAYS,
                listOf(
                  getKeyStroke("K"),
                  getKeyStroke("ctrl K")
                ),
                emptyList()
              ),
              ActionNode(
                "OpenProjectGroup",
                "Unnamed",
                "SEP",
                false,
                When.ALWAYS,
                listOf(getKeyStroke("P")),
                emptyList()
              )
            )
          )
        )
      ),
      ActionNode(
        "ActionsTree.Node.3",
        "b",
        null,
        false,
        When.fileExtension("java"),
        listOf(getKeyStroke("ctrl X")),
        listOf(
          ActionNode(
            "EditorSwapSelectionBoundaries",
            "Unnamed",
            null,
            true,
            When.ALWAYS,
            listOf(getKeyStroke("ctrl X")),
            emptyList()
          )
        )
      ),
      ActionNode(
        "ActionsTree.Node.4",
        "c",
        null,
        false,
        When.any(
          When.fileExtension("txt"),
          When.all(
            When.toolWindowActive("Run"),
            When.fileExtension("java")
          )
        ),
        emptyList(),
        emptyList()
      )
    )

    val actual = ActionNodeParserTest::class.java
      .getResourceAsStream("test.json").use {
        ActionNodeParser.parseJsonActions(it.reader(UTF_8))
      }

    assertEquals(expected, actual)
  }
}
