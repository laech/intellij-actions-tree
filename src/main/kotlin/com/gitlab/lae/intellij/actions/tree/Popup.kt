package com.gitlab.lae.intellij.actions.tree

import com.gitlab.lae.intellij.actions.tree.ui.ActionList
import com.gitlab.lae.intellij.actions.tree.ui.ActionPopupEventDispatcher
import com.gitlab.lae.intellij.actions.tree.ui.ActionPresentation
import com.gitlab.lae.intellij.actions.tree.util.setBestLocation
import com.intellij.ide.DataManager
import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_ESCAPE
import com.intellij.openapi.actionSystem.PlatformDataKeys.CONTEXT_COMPONENT
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.application.ApplicationManager.getApplication
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.JBPopupFactory.ActionSelectionAid
import com.intellij.openapi.ui.popup.PopupChooserBuilder
import com.intellij.openapi.wm.IdeFocusManager
import java.awt.event.ActionEvent
import javax.swing.JComponent
import javax.swing.KeyStroke
import org.jetbrains.concurrency.resolvedPromise

internal class Popup(action: ActionNode, e: AnActionEvent) {

  private val actionManager = e.actionManager
  private val sourceComponent = e.getData(CONTEXT_COMPONENT)
  private val sourceEditor = e.getData(EDITOR)
  private val list: ActionList
  private val popup: JBPopup

  init {
    val items =
        action.prepare(e.dataContext).map { (keys, item) ->
          createPresentation(item, e.actionManager, e.dataContext, keys)
        }
    list = ActionList(items)

    // Register our action first before IntelliJ registers the default
    // actions (e.g. com.intellij.ui.ScrollingUtil) so that in case of
    // conflict our action will be executed
    items.forEach { it.registerShortcuts(list) { item, e -> this.onActionChosen(item, e) } }
    popup = createPopup()
    popup.addListener(
        ActionPopupEventDispatcher(
            popup,
            list,
            IdeEventQueue.getInstance().popupManager,
        ),
    )
    registerIdeAction(ACTION_EDITOR_ESCAPE, popup::cancel)
  }

  fun show(dataContext: DataContext) {
    popup.showInBestPositionFor(dataContext)
  }

  private fun createPresentation(
      action: ActionNode,
      actionManager: ActionManager,
      dataContext: DataContext,
      keysOverride: List<KeyStroke>,
  ): ActionPresentation =
      action.createPresentation(
          actionManager,
          dataContext,
          keysOverride,
      )

  private fun createPopup(): JBPopup =
      PopupChooserBuilder(list)
          .setModalContext(true)
          .setCloseOnEnter(false)
          .setItemChoosenCallback {
            val item = list.selectedValue
            if (item != null) {
              onActionChosen(item, 0)
            }
          }
          .createPopup()

  private fun registerIdeAction(actionId: String, runnable: () -> Unit) {
    val action = actionManager.getAction(actionId) ?: return
    val shortcutSet = action.shortcutSet
    if (shortcutSet.shortcuts.isEmpty()) {
      return
    }

    object : AnAction() {
          override fun actionPerformed(e: AnActionEvent) {
            runnable()
          }
        }
        .registerCustomShortcutSet(shortcutSet, list)
  }

  private fun onActionChosen(item: ActionPresentation, e: ActionEvent) {
    onActionChosen(item, e.modifiers)
  }

  private fun onActionChosen(item: ActionPresentation, modifiers: Int) {
    if (!item.presentation.isEnabled) {
      return
    }

    list.setSelectedValue(item, false)
    val invocation = { performAction(item.action, modifiers) }
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
    getDataContextAsync { context ->
      generateSequence(0, Int::inc).take(list.model.size).map(list.model::getElementAt).forEach {
        it.update(actionManager, context)
      }
    }
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

  private fun performAction(
      action: AnAction,
      modifiers: Int,
      dataContext: DataContext,
  ) {
    val event =
        AnActionEvent(
            null,
            dataContext,
            ActionNode.ACTION_PLACE,
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
