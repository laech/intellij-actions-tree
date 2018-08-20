package com.gitlab.lae.intellij.actions.tree.popup

import com.intellij.openapi.actionSystem.IdeActions.*
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.keymap.KeymapManager
import com.intellij.ui.popup.list.ListPopupImpl
import java.awt.Component
import java.awt.event.ActionEvent
import javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW
import javax.swing.JList
import javax.swing.KeyStroke
import javax.swing.ListCellRenderer

class ActionPopup(component: Component?, items: List<ActionItem>)
    : ListPopupImpl(ActionStep(component, items)) {

    init {
        registerKeymapAction(ACTION_EDITOR_ESCAPE) { cancel() }
        registerKeymapAction(ACTION_EDITOR_MOVE_CARET_DOWN) { select(list, 1) }
        registerKeymapAction(ACTION_EDITOR_MOVE_CARET_UP) { select(list, -1) }
    }

    override fun getListElementRenderer(): ListCellRenderer<*> {
        return ActionRenderer(this)
    }

    fun registerKeyboardAction(keys: List<KeyStroke>, action: (ActionEvent) -> Unit) =
            keys.forEach { key ->
                content.registerKeyboardAction(
                        action, key, WHEN_IN_FOCUSED_WINDOW)
            }

    private fun registerKeymapAction(actionId: String, run: () -> Unit) {
        KeymapManager.getInstance().activeKeymap
                .getShortcuts(actionId)
                .filterIsInstance<KeyboardShortcut>()
                .filter { it.secondKeyStroke == null }
                .map { it.firstKeyStroke }
                .forEach { key ->
                    content.registerKeyboardAction({
                        run()
                    }, key, WHEN_IN_FOCUSED_WINDOW)
                }
    }

    private fun select(list: JList<Any>, increment: Int) {
        val i = list.selectedIndex + increment
        list.selectedIndex = (i + list.model.size) % list.model.size
    }
}
