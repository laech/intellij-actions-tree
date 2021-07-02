package com.gitlab.lae.intellij.actions.tree.util

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import javax.swing.JComponent

fun JBPopup.setBestLocation(
  popupFactory: JBPopupFactory,
  editor: Editor,
) {
  editor.scrollingModel.runActionOnScrollingFinished {
    setLocation(
      popupFactory
        .guessBestPopupLocation(editor)
        .screenPoint,
    )
  }
}

fun JBPopup.setBestLocation(
  popupFactory: JBPopupFactory,
  component: JComponent,
) {
  setLocation(popupFactory.guessBestPopupLocation(component).screenPoint)
}
