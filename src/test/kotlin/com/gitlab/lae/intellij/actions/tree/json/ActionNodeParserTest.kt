package com.gitlab.lae.intellij.actions.tree.json

import com.gitlab.lae.intellij.actions.tree.When.All
import com.gitlab.lae.intellij.actions.tree.When.Any
import com.gitlab.lae.intellij.actions.tree.When.FileExtension
import com.gitlab.lae.intellij.actions.tree.When.ToolWindowActive
import com.gitlab.lae.intellij.actions.tree.actionNode
import com.gitlab.lae.intellij.actions.tree.keys
import kotlin.text.Charsets.UTF_8
import org.junit.Assert.assertEquals
import org.junit.Test

class ActionNodeParserTest {

  @Test
  fun deserialization() {
    val expected =
        listOf(
            actionNode(
                id = "ActionsTree.Node.1",
                name = "Unnamed",
                condition = ToolWindowActive("Project"),
                keys = keys("ctrl C"),
                items =
                    listOf(
                        actionNode(
                            id = "ActionsTree.Node.2",
                            name = "Unnamed",
                            keys = keys("P"),
                            items =
                                listOf(
                                    actionNode(
                                        id = "CloseProject",
                                        name = "Unnamed",
                                        keys = keys("K", "ctrl K"),
                                    ),
                                    actionNode(
                                        id = "OpenProjectGroup",
                                        name = "Unnamed",
                                        separatorAbove = "SEP",
                                        keys = keys("P"),
                                    ),
                                ),
                        ),
                    ),
            ),
            actionNode(
                id = "ActionsTree.Node.3",
                name = "b",
                condition = FileExtension("java"),
                keys = keys("ctrl X"),
                items =
                    listOf(
                        actionNode(
                            id = "EditorSwapSelectionBoundaries",
                            name = "Unnamed",
                            keys = keys("ctrl X"),
                            isSticky = true,
                        ),
                    ),
            ),
            actionNode(
                id = "ActionsTree.Node.4",
                name = "c",
                condition =
                    Any(
                        FileExtension("txt"),
                        All(
                            ToolWindowActive("Run"),
                            FileExtension("java"),
                        ),
                    ),
            ),
            actionNode(
                id = "MyId",
                name = "Unnamed",
            ),
        )

    val actual =
        ActionNodeParserTest::class.java.getResourceAsStream("test.json").use {
          parseJsonActions(it!!.reader(UTF_8))
        }

    assertEquals(expected, actual)
  }
}
