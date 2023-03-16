package com.gitlab.lae.intellij.actions.tree

import com.gitlab.lae.intellij.actions.tree.When.Always
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.Presentation
import org.mockito.kotlin.mock
import java.awt.event.InputEvent
import javax.swing.KeyStroke

fun keys(vararg keys: String): List<KeyStroke> =
  keys.map(KeyStroke::getKeyStroke)

fun actionNode(
  id: String = "",
  name: String? = null,
  separatorAbove: String? = null,
  isSticky: Boolean = false,
  condition: When = Always,
  keys: List<KeyStroke> = emptyList(),
  items: List<ActionNode> = emptyList(),
) = ActionNode(id, name, separatorAbove, isSticky, condition, keys, items)

fun actionEvent(
  input: InputEvent? = null,
  context: DataContext = mock(),
  place: String = "",
  presentation: Presentation = Presentation(),
  actionManager: ActionManager = mock(),
  mask: Int = 0,
) = AnActionEvent(
  input,
  context,
  place,
  presentation,
  actionManager,
  mask,
)
