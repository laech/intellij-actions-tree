package com.gitlab.lae.intellij.actions.tree

import com.gitlab.lae.intellij.actions.tree.ui.ActionPresentation
import com.gitlab.lae.intellij.actions.tree.ui.ActionPresentationStep
import com.gitlab.lae.intellij.actions.tree.util.setBestLocation
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces.ACTION_PLACE_QUICK_LIST_POPUP_ACTION
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys.CONTEXT_COMPONENT
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.application.ApplicationManager.getApplication
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.JBPopupFactory.ActionSelectionAid
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.popup.list.ListPopupImpl
import java.awt.event.ActionEvent
import javax.swing.JComponent
import javax.swing.KeyStroke
import org.jetbrains.concurrency.resolvedPromise

internal class Popup(action: ActionNode, e: AnActionEvent) {

  private val actionManager = e.actionManager
  private val sourceComponent = e.getData(CONTEXT_COMPONENT)
  private val sourceEditor = e.getData(EDITOR)
  private val popup: ListPopupImpl
  private val items: List<ActionPresentation>

  init {
    items =
      action.prepare(e.dataContext).map { (keys, item) ->
        createPresentation(item, e.actionManager, e.dataContext, keys)
      }

    popup =
      ListPopupImpl(
        e.project,
        ActionPresentationStep(items) { value, modifiers -> onActionChosen(value, modifiers) },
      )
    items.forEach { item ->
      item.registerShortcuts(popup.list) { event -> onActionChosen(item, event) }
    }
  }

  fun show(dataContext: DataContext) {
    popup.showInBestPositionFor(dataContext)
  }

  private fun createPresentation(
    action: ActionNode,
    actionManager: ActionManager,
    dataContext: DataContext,
    keysOverride: List<KeyStroke>,
  ): ActionPresentation = action.createPresentation(actionManager, dataContext, keysOverride)

  private fun onActionChosen(item: ActionPresentation, e: ActionEvent) {
    onActionChosen(item, e.modifiers)
  }

  private fun onActionChosen(item: ActionPresentation, modifiers: Int) {
    if (!item.presentation.isEnabled) {
      return
    }

    val invocation = { performAction(item.value, modifiers) }
    if (item.sticky) {
      invocation()
      getApplication().invokeLater {
        updatePopupLocation()
        updatePresentations()
      }
    } else {
      popup.setFinalRunnable(invocation)
      popup.closeOk(null)
    }
  }

  private fun updatePopupLocation() {
    val popupFactory = JBPopupFactory.getInstance()
    if (sourceEditor != null) {
      popup.setBestLocation(popupFactory, sourceEditor)
    } else if (sourceComponent is JComponent) {
      popup.setBestLocation(popupFactory, sourceComponent)
    }
  }

  /**
   * Updates list items to show correct information regarding current state, such as whether an
   * action should be enabled/disabled for current cursor position.
   */
  private fun updatePresentations() {
    getDataContextAsync { context -> items.forEach { it.update(actionManager, context) } }
  }

  private fun getDataContextAsync(consumer: (DataContext) -> Unit) {
    val dataManager = DataManager.getInstance()
    (if (sourceComponent == null) {
        dataManager.dataContextFromFocusAsync
      } else {
        resolvedPromise(dataManager.getDataContext(sourceComponent))
      })
      .then(consumer)
  }

  private fun performAction(action: AnAction, modifiers: Int) {
    /*
     * Wrapping with invokeLater()
     * then IdeFocusManager.doWhenFocusSettlesDown()
     * is required to get back to the pre-popup focus state
     * before executing the action, as some action won't
     * get executed correctly if focus is not restored, such
     * as 'Goto next Splitter'.
     */
    getDataContextAsync { context ->
      getApplication().invokeLater {
        IdeFocusManager.findInstance().doWhenFocusSettlesDown {
          // Another invokeLater() to put us back on the right thread
          getApplication().invokeLater { performAction(action, modifiers, context) }
        }
      }
    }
  }

  private fun performAction(action: AnAction, modifiers: Int, dataContext: DataContext) {
    val event =
      AnActionEvent(
        null,
        dataContext,
        ACTION_PLACE_QUICK_LIST_POPUP_ACTION,
        action.templatePresentation.clone(),
        actionManager,
        modifiers,
      )

    event.setInjectedContext(action.isInInjectedContext)
    if (showPopupIfGroup(action, event)) {
      return
    }

    if (ActionUtil.lastUpdateAndCheckDumb(action, event, false)) {
      ActionUtil.performActionDumbAwareWithCallbacks(action, event)
    }
  }

  private fun showPopupIfGroup(action: AnAction, e: AnActionEvent): Boolean {
    if (action !is ActionGroup) {
      return false
    }
    if (e.presentation.isPerformGroup) {
      return false
    }

    JBPopupFactory.getInstance()
      .createActionGroupPopup(
        e.presentation.text,
        action,
        e.dataContext,
        ActionSelectionAid.NUMBERING,
        true,
      )
      .showInBestPositionFor(e.dataContext)

    return true
  }
}
