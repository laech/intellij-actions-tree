package com.gitlab.lae.intellij.actions.tree

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_ESCAPE
import com.intellij.openapi.actionSystem.PlatformDataKeys.CONTEXT_COMPONENT
import com.intellij.openapi.keymap.KeymapManager
import com.intellij.ui.popup.PopupFactoryImpl.*
import com.intellij.ui.popup.PopupFactoryImpl.ActionGroupPopup.getActionItems
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.KeyStroke

sealed class ActionNode {
    abstract val keys: List<KeyStroke>
}

data class ActionRef(
        override val keys: List<KeyStroke>,
        val id: String) : ActionNode()

data class ActionContainer(
        override val keys: List<KeyStroke>,
        val name: String?,
        val items: List<ActionNode>) : ActionNode()

data class ActionSeparator(val name: String?) : ActionNode() {
    override val keys: List<KeyStroke> get() = emptyList()
}

fun ActionNode.toAction(mgr: ActionManager): AnAction? = when (this) {
    is ActionRef -> mgr.getAction(id)?.let { ActionWrapper(keys, it) }
    is ActionSeparator -> Separator(name)
    is ActionContainer -> ActionWrapper(keys, object : AnAction(name ?: "...") {
        override fun actionPerformed(e: AnActionEvent) = showPopup(e)
        override fun isDumbAware() = true
    })
}

private fun ActionContainer.toActionGroup(mgr: ActionManager) =
        object : ActionGroup(name ?: "...", true) {
            override fun isDumbAware() = true
            override fun getChildren(e: AnActionEvent?) =
                    items.mapNotNull { it.toAction(mgr) }.toTypedArray()
        }

private fun ActionContainer.showPopup(e: AnActionEvent) {
    val component = e.dataContext.getData(CONTEXT_COMPONENT)
    val place = ActionPlaces.UNKNOWN
    val items = getActionItems(
            toActionGroup(e.actionManager),
            e.dataContext, false, false, true, false, place)

    val step = object : ActionPopupStep(items, null, component, false, null, false, true) {
        override fun isSpeedSearchEnabled() = false
    }

    val popup = object : ActionGroupPopup(null, step, null, e.dataContext, place, 30) {
        override fun getListElementRenderer() = ActionRenderer(this)
    }

    // Removes the default behaviour of jumping to an item when keys are typed,
    popup.list.keyListeners
            .filter { it.javaClass.name == "javax.swing.plaf.basic.BasicListUI\$Handler" }
            .forEach { popup.list.removeKeyListener(it) }

    items.forEachIndexed { i, item ->
        item.keys().forEach { key ->
            popup.registerAction("action-$i", key, object : AbstractAction() {
                override fun actionPerformed(e: ActionEvent) {
                    popup.list.selectedIndex = i
                    popup.handleSelect(true)
                }
            })
        }
    }

    KeymapManager.getInstance().activeKeymap
            .getShortcuts(ACTION_EDITOR_ESCAPE)
            .filterIsInstance<KeyboardShortcut>()
            .filter { it.secondKeyStroke == null }
            .map { it.firstKeyStroke }
            .forEach { key ->
                popup.registerAction("cancel", key, object : AbstractAction() {
                    override fun actionPerformed(e: ActionEvent) {
                        popup.cancel()
                    }
                })
            }

    popup.showInBestPositionFor(e.dataContext)
}

fun ActionItem.keys() = action.shortcutSet.shortcuts
        .asSequence()
        .filterIsInstance<KeyboardShortcut>()
        .filter { it.secondKeyStroke == null }
        .map { it.firstKeyStroke }
        .toList()
