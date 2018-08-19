package com.gitlab.lae.intellij.actions.tree

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.Presentation
import javax.swing.KeyStroke

private const val HAS_SUB_STEP = "has_sub_step"
private const val SEP_ABOVE = "sep_above"

data class ActionPresentation(
        val action: AnAction,
        val keys: List<KeyStroke>,
        val presentation: Presentation) {

    var hasSubstep: Boolean
        get() = presentation.getClientProperty(HAS_SUB_STEP) ?: false == true
        set(value) = presentation.putClientProperty(HAS_SUB_STEP, value)

    var sepAbove: Boolean
        get() = presentation.getClientProperty(SEP_ABOVE) ?: false == true
        set(value) = presentation.putClientProperty(SEP_ABOVE, value)
}
