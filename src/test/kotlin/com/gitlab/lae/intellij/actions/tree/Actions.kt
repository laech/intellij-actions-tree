package com.gitlab.lae.intellij.actions.tree

import com.gitlab.lae.intellij.actions.tree.app.RootAction
import com.intellij.openapi.actionSystem.*
import org.mockito.kotlin.mock
import java.awt.event.InputEvent
import javax.swing.KeyStroke

fun keys(vararg keys: String): List<KeyStroke> =
  keys.map(KeyStroke::getKeyStroke)

fun action(
  id: String = "",
  name: String? = null,
  separatorAbove: String? = null,
  isSticky: Boolean = false,
  condition: When = When.ALWAYS,
  keys: List<KeyStroke> = emptyList(),
  items: List<ActionNode> = emptyList(),
) = ActionNode(id, name, separatorAbove, isSticky, condition, keys, items)

fun rootAction(
  id: String = "",
  keys: List<KeyStroke> = emptyList(),
  actions: List<Pair<AnAction, When>> = emptyList(),
) = RootAction(id, keys, actions)

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
