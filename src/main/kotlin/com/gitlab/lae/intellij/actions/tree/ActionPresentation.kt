package com.gitlab.lae.intellij.actions.tree

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.ui.popup.ListSeparator
import javax.swing.KeyStroke

data class ActionPresentation(
        val action: AnAction,
        val keys: List<KeyStroke>,
        val name: String?,
        val description: String?,
        val hasChildren: Boolean,
        val isEnabled: Boolean,
        val separator: ListSeparator?)
