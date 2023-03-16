package com.gitlab.lae.intellij.actions.tree.app

import com.gitlab.lae.intellij.actions.tree.ActionNode
import com.gitlab.lae.intellij.actions.tree.util.setEnabledModalContext
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CustomShortcutSet
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.actionSystem.ShortcutSet
import javax.swing.KeyStroke

data class RootAction(
  val id: String,
  val keyStrokes: List<KeyStroke>,
  val actions: List<ActionNode>,
) : AnAction() {

  init {
    super.setShortcutSet(
      CustomShortcutSet(
        *keyStrokes.map { KeyboardShortcut(it, null) }.toTypedArray(),
      ),
    )
  }

  override fun getActionUpdateThread() = ActionUpdateThread.EDT

  override fun setShortcutSet(ignored: ShortcutSet) {}

  private fun findAction(e: AnActionEvent): AnAction? = actions
    .asSequence()
    .filter { it.condition.test(e.dataContext) }
    .map { it.toAction(e.actionManager) }
    .firstOrNull()

  override fun actionPerformed(e: AnActionEvent) {
    findAction(e)?.actionPerformed(e)
  }

  override fun beforeActionPerformedUpdate(e: AnActionEvent) {
    super.beforeActionPerformedUpdate(e)
    findAction(e)?.beforeActionPerformedUpdate(e)
  }

  override fun update(e: AnActionEvent) {
    super.update(e)

    /* It's important to disable this action when none inner action
     * is available to execute, so that the keystroke will be passed
     * through to the IDE for processing.
     *
     * For example, if the 'N' key is configured to be the 'Down'
     * action when the Project tool window is active, it should do
     * that when the Project tool window is active, but when the editor
     * is active, it should still insert the 'N' character, because
     * the 'N' key is not bounded to any action when the editor is
     * active, disabling the action correctly allows this behaviour.
     */
    val action = findAction(e)
    e.presentation.isEnabled = action != null
    if (action != null) {
      action.update(e)
      setEnabledModalContext(e, action)
    }
  }

  override fun isDumbAware() = true

  companion object {
    /**
     * Merges the root level actions by their keystrokes.
     *
     *
     * For example:
     * ```
     * ActionNode{keyStrokes=["ctrl X"], id=1 ...}
     * ActionNode{keyStrokes=["ctrl X", "ctrl Y"], id=2 ...}
     * ActionNode{keyStrokes=["ctrl A", "ctrl B"], id=3 ...}
     * ```
     * Becomes:
     * ```
     * RootAction{keyStrokes=["ctrl X"], actions=[{id=2, ...}, {id=1, ...}]}
     * RootAction{keyStrokes=["ctrl Y"], actions=[{id=2, ...}]}
     * RootAction{keyStrokes=["ctrl A", "ctrl B"], actions=[{id=3, ...}]}
     * ```
     * The order of the inner actions of each [RootAction] is reverse.
     */
    fun merge(
      actions: Collection<ActionNode>,
      actionManager: ActionManager,
    ): List<RootAction> {

      val (noKeys, byKeys) = actions
        .partition { it.keys.isEmpty() }
        .let { (noKeys, hasKeys) ->
          noKeys to hasKeys.asSequence()
            .flatMap { it.keys.asSequence().map { key -> key to it } }
            .groupBy { it.first }
            .mapValues { it.value.map { p -> p.second } }
        }

      val byActions = byKeys.entries
        .groupBy { it.value }
        .mapValues { it.value.map { e -> e.key } } +
        (if (noKeys.isEmpty()) emptyMap()
        else mapOf(noKeys to emptyList()))

      return generateSequence(0, Int::inc)
        .zip(byActions.asSequence()) { i, (k, v) -> Triple(i, k, v) }
        .map { (i, nodes, keys) ->
          /*
           * If list contains a single action group, use that group's
           * ID as the action ID, this allows user specified IDs to be
           * maintained and referenced in other plugins such as IdeaVim's
           * :action command if they wish.
           *
           * We only do this for custom action groups and not for references
           * to existing action items because it will result in duplicate
           * ID error when registering later.
           */
          val id = if (nodes.size == 1 && nodes[0].items.isNotEmpty()) {
            nodes[0].id
          } else {
            "ActionsTree.Root.$i"
          }

          RootAction(id, keys, nodes.reversed()).apply {
            this.templatePresentation.setText {
              this.actions.joinToString(", ") {
                it.toAction(actionManager).templatePresentation.text ?: ""
              }
            }
            this.isEnabledInModalContext = this.actions.any {
              it.toAction(actionManager).isEnabledInModalContext
            }
          }
        }.toList()
    }
  }
}
