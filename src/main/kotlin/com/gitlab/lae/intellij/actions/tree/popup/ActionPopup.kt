package com.gitlab.lae.intellij.actions.tree.popup

import com.intellij.ui.popup.list.ListPopupImpl
import java.awt.Component
import javax.swing.ListCellRenderer

class ActionPopup(component: Component?, items: List<ActionItem>)
    : ListPopupImpl(ActionStep(component, items)) {

    override fun getListElementRenderer(): ListCellRenderer<*> {
        return ActionRenderer(this)
    }
}
