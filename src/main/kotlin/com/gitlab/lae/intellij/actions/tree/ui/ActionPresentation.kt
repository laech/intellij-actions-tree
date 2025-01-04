package com.gitlab.lae.intellij.actions.tree.ui

import com.gitlab.lae.intellij.actions.tree.util.setEnabledModalContext
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces.ACTION_PLACE_QUICK_LIST_POPUP_ACTION
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.AnActionHolder
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ShortcutProvider
import com.intellij.openapi.actionSystem.ex.ActionUtil.performDumbAwareUpdate
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder
import com.intellij.ui.popup.list.PopupListElementRenderer.CUSTOM_KEY_STROKE_TEXT
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.JList
import javax.swing.KeyStroke

data class ActionPresentation(
  val presentation: Presentation,
  val keys: List<KeyStroke>,
  val separatorAbove: String?,
  val sticky: Boolean,
  val value: AnAction,
) : ShortcutProvider, AnActionHolder {

  override fun toString() = presentation.text ?: ""

  // Inherited from ShortcutProvider and returns null, so that IntelliJ's builtin
  // PopupListElementRenderer will fall back to look for UserDataHolder to get custom shortcut text
  @Deprecated("Don't use directly", ReplaceWith("")) override fun getShortcut() = null

  // Inherited from AnActionHolder and return value implements UserDataHolder, to provide keyboard
  // shortcut label to PopupListElementRenderer, as its default behaviour doesn't create proper
  // labels for key typed strokes
  @Deprecated("Don't use directly", ReplaceWith("getValue"))
  override fun getAction(): AnAction {
    return object : AnAction(), UserDataHolder {

      override fun actionPerformed(e: AnActionEvent) = Unit

      override fun <T> putUserData(key: Key<T>, value: T?) = Unit

      override fun <T> getUserData(key: Key<T>) =
        if (key == CUSTOM_KEY_STROKE_TEXT) {
          @Suppress("UNCHECKED_CAST")
          keys.joinToString(", ") { getKeyText(it) } as T
        } else {
          null
        }
    }
  }

  fun registerShortcuts(list: JList<*>, handler: (ActionEvent) -> Unit) {
    if (keys.isEmpty()) {
      return
    }
    val inputMap = list.inputMap
    val actionMap = list.actionMap
    for (key in keys) {
      inputMap.put(key, key)
      actionMap.put(
        key,
        object : AbstractAction() {
          override fun actionPerformed(e: ActionEvent) {
            handler(e)
          }
        },
      )
    }
  }

  fun update(actionManager: ActionManager, dataContext: DataContext) {
    val event =
      AnActionEvent(
        dataContext,
        presentation,
        ACTION_PLACE_QUICK_LIST_POPUP_ACTION,
        ActionUiKind.POPUP,
        null,
        0,
        actionManager,
      )
    event.setInjectedContext(value.isInInjectedContext)
    performDumbAwareUpdate(value, event, false)
    setEnabledModalContext(event, value)
  }

  companion object {
    fun create(action: AnAction, keys: List<KeyStroke>, separatorAbove: String?, sticky: Boolean) =
      ActionPresentation(action.templatePresentation.clone(), keys, separatorAbove, sticky, action)
  }
}
