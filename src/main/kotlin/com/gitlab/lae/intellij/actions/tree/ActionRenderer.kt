package com.gitlab.lae.intellij.actions.tree

import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.ui.popup.PopupFactoryImpl.ActionItem
import com.intellij.ui.popup.list.ListPopupImpl
import com.intellij.ui.popup.list.PopupListElementRenderer
import javax.swing.JLabel
import javax.swing.JList

class ActionRenderer(list: ListPopupImpl)
    : PopupListElementRenderer<ActionItem>(list) {

    // TODO remove reflection

    override fun customizeComponent(
            list: JList<out ActionItem>,
            value: ActionItem,
            isSelected: Boolean
    ) {
        super.customizeComponent(list, value, isSelected)
        updateShortcutText(value)
    }

    private fun updateShortcutText(value: ActionItem) {
        val keys = value.keys()
        if (keys.size < 2) return
        if (myShortcutLabelField == null) return
        try {
            val field = myShortcutLabelField.get(this) as JLabel
            field.text = keys.joinToString(
                    separator = " ",
                    prefix = "     ",
                    transform = KeymapUtil::getKeystrokeText)
        } catch (ignore: Exception) {
        }
    }

    companion object {
        private val myShortcutLabelField = try {
            val field = PopupListElementRenderer::class
                    .java.getDeclaredField("myShortcutLabel")
            field.isAccessible = true
            field
        } catch (e: Exception) {
            null
        }
    }
}
