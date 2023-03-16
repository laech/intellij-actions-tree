package com.gitlab.lae.intellij.actions.tree

import com.gitlab.lae.intellij.actions.tree.ui.ActionPresentation
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DataContext
import javax.swing.KeyStroke

data class ActionNode(
    val id: String,
    val name: String?,
    val separatorAbove: String?,
    val isSticky: Boolean,
    val condition: When,
    val keys: List<KeyStroke>,
    val items: List<ActionNode>,
) {

  fun createPresentation(
      actionManager: ActionManager,
      dataContext: DataContext,
      keysOverride: List<KeyStroke>,
  ): ActionPresentation {

    val action = toAction(actionManager)
    val presentation =
        ActionPresentation.create(
            action,
            keysOverride,
            separatorAbove,
            isSticky,
        )
    presentation.update(actionManager, dataContext)
    return presentation
  }

  fun toAction(actionManager: ActionManager): AnAction =
      when {
        items.isEmpty() -> actionManager.getAction(id) ?: UnknownAction(this)
        else -> PopupAction(this)
      }

  /**
   * Prepares the child items for the given context.
   *
   * Returns a list of key strokes and actions pairs. If multiple actions were mapped to the same
   * key strokes originally, then the last action whose [ActionNode.when] evaluates to true will be
   * chosen.
   */
  fun prepare(context: DataContext): List<Pair<List<KeyStroke>, ActionNode>> {
    val registered = HashSet<KeyStroke>()
    return items
        .reversed()
        .asSequence()
        .filter { it.condition.test(context) }
        .map { it.keys.filter { k -> registered.add(k) } to it }
        .toList()
        .reversed()
  }

  companion object {

    /* Use ACTION_SEARCH as the action place seems to work the best.
     *
     * 'Run | Stop' menu action works correctly this way by
     * showing a list of processes to stop
     *
     * 'Exit' actions works (doesn't work if place is MAIN_MENU)
     */
    const val ACTION_PLACE = ActionPlaces.ACTION_SEARCH
  }
}
