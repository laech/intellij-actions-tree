package com.gitlab.lae.intellij.actions.tree.app

import com.gitlab.lae.intellij.actions.tree.*
import com.gitlab.lae.intellij.actions.tree.When.ALWAYS
import com.gitlab.lae.intellij.actions.tree.When.NEVER
import com.intellij.ide.DataManager
import com.intellij.ide.IdePopupManager
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.IdeActions.*
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.IdeFocusManager
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.*
import org.junit.Test

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
    val action = rootAction(actions = listOf(enable to condition))

    val presentation = Presentation()
    presentation.isEnabled = true
    assertTrue(presentation.isEnabled)

    enable.enabled = true
    whenever(condition.test(any())).thenReturn(false)

    val event = actionEvent(presentation = presentation)
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
    val actual = merge(
      listOf(
        action(
          id = id,
          keys = keys("X"),
          items = listOf(action("bob"))
        )
      )
    )
    assertEquals(1, actual.size.toLong())
    assertEquals(id, actual[0].id)
  }

  @Test
  fun `merges root actions`() {
    val cut = EmptyAction("cut", null, null)
    val copy = EmptyAction("copy", null, null)
    val paste = EmptyAction("paste", null, null)

    val actionManager = mock<ActionManager>()
    whenever(actionManager.getAction(ACTION_CUT)).thenReturn(cut)
    whenever(actionManager.getAction(ACTION_COPY)).thenReturn(copy)
    whenever(actionManager.getAction(ACTION_PASTE)).thenReturn(paste)

    val actual = merge(
      listOf(
        action(
          id = ACTION_CUT,
          keys = keys("typed a")
        ),
        action(
          id = ACTION_COPY,
          keys = keys("typed a", "typed b"),
          condition = When.toolWindowActive("Project")
        ),
        action(
          id = ACTION_PASTE,
          keys = keys("typed x", "typed y"),
          condition = When.fileExtension("txt")
        )
      ),
      actionManager
    )

    val expected = listOf(
      RootAction(
        "ActionsTree.Root.0",
        keys("typed a"),
        listOf(
          copy to When.toolWindowActive("Project"),
          cut to ALWAYS
        )
      ),
      RootAction(
        "ActionsTree.Root.1",
        keys("typed b"),
        listOf(copy to When.toolWindowActive("Project"))
      ),
      RootAction(
        "ActionsTree.Root.2",
        keys("typed x", "typed y"),
        listOf(paste to When.fileExtension("txt"))
      )
    )
    assertEquals(expected, actual)
  }

  @Test
  fun `merging maintains action with no key strokes`() {
    val cut = EmptyAction("cut", null, null)
    val copy = EmptyAction("copy", null, null)
    val paste = EmptyAction("paste", null, null)
    val actionManager = mock<ActionManager>()
    whenever(actionManager.getAction(ACTION_CUT)).thenReturn(cut)
    whenever(actionManager.getAction(ACTION_COPY)).thenReturn(copy)
    whenever(actionManager.getAction(ACTION_PASTE)).thenReturn(paste)

    val actual = merge(
      listOf(
        action(ACTION_CUT),
        action(ACTION_COPY, keys = keys("typed b")),
        action(ACTION_PASTE)
      ),
      actionManager
    )

    val expected = listOf(
      rootAction(
        id = "ActionsTree.Root.0",
        keys = keys("typed b"),
        actions = listOf(copy to ALWAYS)
      ),
      rootAction(
        id = "ActionsTree.Root.1",
        actions = listOf(
          paste to ALWAYS,
          cut to ALWAYS
        )
      )
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
    assertTrue(
      rootAction(
        actions = listOf(
          ModalAction(true) to NEVER,
          ModalAction(false) to NEVER
        )
      ).isEnabledInModalContext
    )
    assertFalse(
      rootAction(
        actions = listOf(
          ModalAction(false) to NEVER,
          ModalAction(false) to NEVER
        )
      ).isEnabledInModalContext
    )
  }

  private fun merge(
    actions: List<ActionNode>,
    actionManager: ActionManager = mock(),
    focusManager: IdeFocusManager = mock(),
    popupManager: IdePopupManager = IdePopupManager(),
    popupFactory: JBPopupFactory = mock(),
    dataManager: DataManager = mock()
  ) = RootAction.merge(
    actions,
    actionManager,
    focusManager,
    popupManager,
    popupFactory,
    dataManager
  )
}
