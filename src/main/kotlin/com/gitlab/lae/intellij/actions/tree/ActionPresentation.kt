package com.gitlab.lae.intellij.actions.tree

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.Presentation
import javax.swing.KeyStroke

class ActionPresentation(
        val presentation: Presentation,
        val keys: List<KeyStroke>,
        val separatorAbove: String?,
        val sticky: Boolean,
        val action: AnAction) {

    override fun toString() = presentation.text ?: ""
}
