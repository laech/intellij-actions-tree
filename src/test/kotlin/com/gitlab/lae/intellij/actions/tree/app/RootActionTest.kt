package com.gitlab.lae.intellij.actions.tree.app

import com.gitlab.lae.intellij.actions.tree.When
import com.gitlab.lae.intellij.actions.tree.When.FileExtension
import com.gitlab.lae.intellij.actions.tree.When.ToolWindowActive
import com.gitlab.lae.intellij.actions.tree.actionEvent
import com.gitlab.lae.intellij.actions.tree.actionNode
import com.gitlab.lae.intellij.actions.tree.keys
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.EmptyAction
import com.intellij.openapi.actionSystem.IdeActions.ACTION_COPY
import com.intellij.openapi.actionSystem.IdeActions.ACTION_CUT
import com.intellij.openapi.actionSystem.IdeActions.ACTION_PASTE
import com.intellij.openapi.actionSystem.Presentation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class RootActionTest {

  private class EnableAction : AnAction() {
    var enabled = false

    override fun actionPerformed(e: AnActionEvent) {}

    override fun update(e: AnActionEvent) {
      super.update(e)
      e.presentation.isEnabled = enabled
    }
  }

  @Test
  fun `disables presentation if no suitable action found`() {
    val enable = EnableAction()
    val condition = mock<When>()
    val actionManager = mock<ActionManager>()
    whenever(actionManager.getAction("bob")).thenReturn(enable)

    val action =
        RootAction.merge(
                listOf(actionNode("bob", condition = condition)),
                actionManager,
            )[0]

    val presentation = Presentation()
    presentation.isEnabled = true
    assertTrue(presentation.isEnabled)

    enable.enabled = true
    whenever(condition.test(any())).thenReturn(false)

    val event =
        actionEvent(
            presentation = presentation,
            actionManager = actionManager,
        )
    action.update(event)
    assertFalse(presentation.isEnabled)

    whenever(condition.test(any())).thenReturn(true)
    action.update(event)
    assertTrue(presentation.isEnabled)

    enable.enabled = false
    action.update(event)
    assertFalse(presentation.isEnabled)
  }

  @Test
  fun `merging maintains custom action groups ids`() {
    val id = "my-custom-group-id"
    val actual =
        RootAction.merge(
            listOf(
                actionNode(
                    id = id,
                    keys = keys("X"),
                    items = listOf(actionNode("bob")),
                ),
            ),
            mock(),
        )
    assertEquals(1, actual.size.toLong())
    assertEquals(id, actual[0].id)
  }

  @Test
  fun `merges root actions`() {
    val cut = EmptyAction("Cut", null, null)
    val copy = EmptyAction("Copy", null, null)
    val paste = EmptyAction("Paste", null, null)

    val actionManager = mock<ActionManager>()
    whenever(actionManager.getAction(ACTION_CUT)).thenReturn(cut)
    whenever(actionManager.getAction(ACTION_COPY)).thenReturn(copy)
    whenever(actionManager.getAction(ACTION_PASTE)).thenReturn(paste)

    val cutNode = actionNode(ACTION_CUT, keys = keys("typed a"))
    val copyNode =
        actionNode(
            ACTION_COPY,
            keys = keys("typed a", "typed b"),
            condition = ToolWindowActive("Project"),
        )
    val pasteNode =
        actionNode(
            ACTION_PASTE,
            keys = keys("typed x", "typed y"),
            condition = FileExtension("txt"),
        )

    val actual = RootAction.merge(listOf(cutNode, copyNode, pasteNode), actionManager)

    val expected =
        listOf(
            RootAction(
                "ActionsTree.Root.0",
                keys("typed a"),
                listOf(copyNode, cutNode),
            ),
            RootAction(
                "ActionsTree.Root.1",
                keys("typed b"),
                listOf(copyNode),
            ),
            RootAction(
                "ActionsTree.Root.2",
                keys("typed x", "typed y"),
                listOf(pasteNode),
            ),
        )
    assertEquals(expected, actual)
  }

  @Test
  fun `merging maintains action with no key strokes`() {
    val cut = EmptyAction("Cut", null, null)
    val copy = EmptyAction("Copy", null, null)
    val paste = EmptyAction("Paste", null, null)
    val actionManager = mock<ActionManager>()
    whenever(actionManager.getAction(ACTION_CUT)).thenReturn(cut)
    whenever(actionManager.getAction(ACTION_COPY)).thenReturn(copy)
    whenever(actionManager.getAction(ACTION_PASTE)).thenReturn(paste)

    val cutNode = actionNode(ACTION_CUT)
    val copyNode = actionNode(ACTION_COPY, keys = keys("typed b"))
    val pasteNode = actionNode(ACTION_PASTE)

    val actual = RootAction.merge(listOf(cutNode, copyNode, pasteNode), actionManager)

    val expected =
        listOf(
            RootAction(
                "ActionsTree.Root.0",
                keys("typed b"),
                listOf(copyNode),
            ),
            RootAction(
                "ActionsTree.Root.1",
                emptyList(),
                listOf(pasteNode, cutNode),
            ),
        )
    assertEquals(expected, actual)
  }

  private class ModalAction(enableInModalContext: Boolean) : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {}

    init {
      isEnabledInModalContext = enableInModalContext
    }
  }

  @Test
  fun `enable in modal if any action supports modal`() {
    val modalTrue = ModalAction(true)
    val modalFalse = ModalAction(false)

    val actionManager = mock<ActionManager>()
    whenever(actionManager.getAction("true")).thenReturn(modalTrue)
    whenever(actionManager.getAction("false")).thenReturn(modalFalse)

    assertTrue(
        RootAction.merge(
                listOf(
                    actionNode("true"),
                    actionNode("false"),
                ),
                actionManager,
            )[0]
            .isEnabledInModalContext,
    )

    assertFalse(
        RootAction.merge(
                listOf(
                    actionNode("false"),
                    actionNode("false"),
                ),
                actionManager,
            )[0]
            .isEnabledInModalContext,
    )
  }
}
