package com.gitlab.lae.intellij.actions.tree.app

import com.gitlab.lae.intellij.actions.tree.ActionNode
import com.gitlab.lae.intellij.actions.tree.When
import com.intellij.ide.DataManager
import com.intellij.ide.IdePopupManager
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.IdeActions.*
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.IdeFocusManager
import org.junit.Assert.*
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import javax.swing.KeyStroke
import javax.swing.KeyStroke.getKeyStroke

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
    val condition = mock(When::class.java)
    val action = RootAction(
      "id",
      emptyList(),
      listOf(enable to condition)
    )

    val presentation = Presentation()
    presentation.isEnabled = true
    val event = AnActionEvent(
      null,
      mock(DataContext::class.java),
      "",
      presentation,
      mock(ActionManager::class.java),
      0
    )
    assertTrue(presentation.isEnabled)

    enable.enabled = true
    `when`(condition.test(any())).thenReturn(false)
    action.update(event)
    assertFalse(presentation.isEnabled)

    `when`(condition.test(any())).thenReturn(true)
    action.update(event)
    assertTrue(presentation.isEnabled)

    enable.enabled = false
    action.update(event)
    assertFalse(presentation.isEnabled)
  }

  @Test
  fun `merging maintains custom action groups ids`() {
    val id = "my-custom-group-id"
    val actual = RootAction.merge(
      listOf(
        newActionNode(
          id,
          When.ALWAYS,
          listOf(getKeyStroke("X")),
          listOf(
            newActionNode(
              "bob",
              When.ALWAYS,
              emptyList(),
              emptyList()
            )
          )
        )
      ),
      mock(ActionManager::class.java),
      mock(IdeFocusManager::class.java),
      IdePopupManager(),
      mock(JBPopupFactory::class.java),
      mock(DataManager::class.java)
    )
    assertEquals(1, actual.size.toLong())
    assertEquals(id, actual[0].id)
  }

  @Test
  fun `merges root actions`() {
    val cut = EmptyAction("cut", null, null)
    val copy = EmptyAction("copy", null, null)
    val paste = EmptyAction("paste", null, null)
    val actionManager = mock(ActionManager::class.java)
    `when`(actionManager.getAction(ACTION_CUT)).thenReturn(cut)
    `when`(actionManager.getAction(ACTION_COPY)).thenReturn(copy)
    `when`(actionManager.getAction(ACTION_PASTE)).thenReturn(paste)
    val actual = RootAction.merge(
      listOf(
        newActionNode(
          ACTION_CUT,
          When.ALWAYS,
          listOf(getKeyStroke('a')),
          emptyList()
        ),
        newActionNode(
          ACTION_COPY,
          When.toolWindowActive("Project"),
          listOf(getKeyStroke('a'), getKeyStroke('b')),
          emptyList()
        ),
        newActionNode(
          ACTION_PASTE,
          When.fileExtension("txt"),
          listOf(getKeyStroke('x'), getKeyStroke('y')),
          emptyList()
        )
      ),
      actionManager,
      mock(IdeFocusManager::class.java),
      IdePopupManager(),
      mock(JBPopupFactory::class.java),
      mock(DataManager::class.java)
    )

    val expected = listOf(
      RootAction(
        "ActionsTree.Root.0",
        listOf(getKeyStroke('a')),
        listOf(
          copy to When.toolWindowActive("Project"),
          cut to When.ALWAYS
        )
      ),
      RootAction(
        "ActionsTree.Root.1",
        listOf(getKeyStroke('b')),
        listOf(copy to When.toolWindowActive("Project"))
      ),
      RootAction(
        "ActionsTree.Root.2",
        listOf(getKeyStroke('x'), getKeyStroke('y')),
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
    val actionManager = mock(ActionManager::class.java)
    `when`(actionManager.getAction(ACTION_CUT)).thenReturn(cut)
    `when`(actionManager.getAction(ACTION_COPY)).thenReturn(copy)
    `when`(actionManager.getAction(ACTION_PASTE)).thenReturn(paste)

    val actual = RootAction.merge(
      listOf(
        newActionNode(
          ACTION_CUT,
          When.ALWAYS,
          emptyList(),
          emptyList()
        ),
        newActionNode(
          ACTION_COPY,
          When.ALWAYS,
          listOf(getKeyStroke('b')),
          emptyList()
        ),
        newActionNode(
          ACTION_PASTE,
          When.ALWAYS,
          emptyList(),
          emptyList()
        )
      ),
      actionManager,
      mock(IdeFocusManager::class.java),
      IdePopupManager(),
      mock(JBPopupFactory::class.java),
      mock(DataManager::class.java)
    )

    val expected = listOf(
      RootAction(
        "ActionsTree.Root.0",
        listOf(getKeyStroke('b')),
        listOf(copy to When.ALWAYS)
      ),
      RootAction(
        "ActionsTree.Root.1",
        emptyList(),
        listOf(
          paste to When.ALWAYS,
          cut to When.ALWAYS
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
      RootAction(
        "id",
        emptyList(),
        listOf(
          ModalAction(true) to When.NEVER,
          ModalAction(false) to When.NEVER
        )
      ).isEnabledInModalContext
    )
    assertFalse(
      RootAction(
        "id",
        emptyList(),
        listOf(
          ModalAction(false) to When.NEVER,
          ModalAction(false) to When.NEVER
        )
      ).isEnabledInModalContext
    )
  }

  private fun newActionNode(
    id: String,
    condition: When,
    keyStrokes: List<KeyStroke>,
    items: List<ActionNode>
  ) = ActionNode(
    id,
    null,
    null,
    false,
    condition,
    keyStrokes,
    items
  )
}
