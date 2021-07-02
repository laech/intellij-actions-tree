package com.gitlab.lae.intellij.actions.tree.json

import com.gitlab.lae.intellij.actions.tree.When
import com.gitlab.lae.intellij.actions.tree.action
import com.gitlab.lae.intellij.actions.tree.keys
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.text.Charsets.UTF_8

class ActionNodeParserTest {

  @Test
  fun deserialization() {
    val expected = listOf(
      action(
        id = "ActionsTree.Node.1",
        name = "Unnamed",
        condition = When.toolWindowActive("Project"),
        keys = keys("ctrl C"),
        items = listOf(
          action(
            id = "ActionsTree.Node.2",
            name = "Unnamed",
            keys = keys("P"),
            items = listOf(
              action(
                id = "CloseProject",
                name = "Unnamed",
                keys = keys("K", "ctrl K"),
              ),
              action(
                id = "OpenProjectGroup",
                name = "Unnamed",
                separatorAbove = "SEP",
                keys = keys("P"),
              ),
            ),
          ),
        ),
      ),
      action(
        id = "ActionsTree.Node.3",
        name = "b",
        condition = When.fileExtension("java"),
        keys = keys("ctrl X"),
        items = listOf(
          action(
            id = "EditorSwapSelectionBoundaries",
            name = "Unnamed",
            keys = keys("ctrl X"),
            isSticky = true,
          ),
        ),
      ),
      action(
        id = "ActionsTree.Node.4",
        name = "c",
        condition = When.any(
          When.fileExtension("txt"),
          When.all(
            When.toolWindowActive("Run"),
            When.fileExtension("java"),
          ),
        ),
      ),
    )

    val actual = ActionNodeParserTest::class.java
      .getResourceAsStream("test.json").use {
        parseJsonActions(it!!.reader(UTF_8))
      }

    assertEquals(expected, actual)
  }
}
