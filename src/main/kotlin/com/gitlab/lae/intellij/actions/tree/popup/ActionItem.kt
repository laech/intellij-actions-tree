package com.gitlab.lae.intellij.actions.tree.popup

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.CustomShortcutSet
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.actionSystem.ShortcutProvider
import com.intellij.openapi.ui.popup.ListSeparator
import javax.swing.KeyStroke

data class ActionItem(
        val action: AnAction,
        val keys: List<KeyStroke>,
        val name: String?,
        val description: String?,
        val isEnabled: Boolean,
        val hasChildren: Boolean,
        val separator: ListSeparator?) : ShortcutProvider {

    override fun getShortcut() = CustomShortcutSet(*keys
            .map { KeyboardShortcut(it, null) }
            .toTypedArray())
}
