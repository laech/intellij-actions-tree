package com.gitlab.lae.intellij.actions.tree.app

import com.gitlab.lae.intellij.actions.tree.ActionNode
import com.gitlab.lae.intellij.actions.tree.json.ActionNodeParser
import com.google.common.base.Throwables.getStackTraceAsString
import com.intellij.ide.DataManager
import com.intellij.ide.IdeEventQueue
import com.intellij.ide.IdePopupManager
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.components.BaseComponent
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.keymap.Keymap
import com.intellij.openapi.keymap.ex.KeymapManagerEx
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.IdeFocusManager
import java.nio.file.Paths
import java.util.*
import java.util.function.Consumer

class AppComponent : BaseComponent {

  private val actionIds = HashSet<String>()

  override fun initComponent() {
    reload()
  }

  fun reload() {
    reload(
      ActionManager.getInstance(),
      IdeFocusManager.findInstance(),
      IdeEventQueue.getInstance().popupManager,
      JBPopupFactory.getInstance(),
      DataManager.getInstance(),
      KeymapManagerEx.getInstanceEx(),
      PropertiesComponent.getInstance()
    )
  }

  private fun reload(
    actionManager: ActionManager,
    focusManager: IdeFocusManager,
    popupManager: IdePopupManager,
    popupFactory: JBPopupFactory,
    dataManager: DataManager,
    keymapManager: KeymapManagerEx,
    properties: PropertiesComponent
  ) {
    val actions = RootAction.merge(
      loadActions(properties),
      actionManager,
      focusManager,
      popupManager,
      popupFactory,
      dataManager
    )

    val keymaps = keymapManager.allKeymaps
    for (keymap in keymaps) {
      actionIds.forEach(Consumer { actionId: String? ->
        keymap.removeAllActionShortcuts(
          actionId!!
        )
      })
    }

    actionIds.forEach(Consumer { actionId ->
      actionManager.unregisterAction(actionId)
    })
    actionIds.clear()

    val pluginId = PluginId.getId(PLUGIN_ID)
    for (action in actions) {
      actionIds.add(action.id)
      actionManager.registerAction(action.id, action, pluginId)
    }

    setShortcuts(keymaps, actions)
  }

  private fun loadActions(properties: PropertiesComponent): List<ActionNode> {
    val conf = properties.getValue(AppConfigurable.CONF_KEY)?.trim()
      ?: return emptyList()

    return try {
      ActionNodeParser.parseJsonActions(Paths.get(conf))
    } catch (e: Exception) {
      Notifications.Bus.notify(
        Notification(
          "ActionsTree",
          "Failed to load keymap",
          "Failed to load keymap: ${conf}\n${getStackTraceAsString(e)}",
          NotificationType.ERROR
        )
      )
      emptyList()
    }
  }

  private fun setShortcuts(
    keymaps: Array<Keymap>,
    actions: Iterable<RootAction>
  ) {
    // Adding a shortcut this way will have it appear as default
    // when viewing the Keymap preferences tree, which is good,
    // don't want it to appear as changed (blue highlight) which will
    // cause it to be saved to user's keymap file (don't want that) on
    // disk (if user has configured a custom keymap)

    // IntelliJ automatically adds a Mac version of the shortcut
    // to the Mac keymaps when a shortcut is added to a parent keymap,
    // for example, adding 'ctrl X' to the $default (root) keymap will
    // cause the Mac keymaps to inherit 'meta X', this is undesirable,
    // clear it and make sure 'ctrl X' is added instead.
    for (keymap in keymaps) {
      for (action in actions) {
        keymap.removeAllActionShortcuts(action.id)
        for (shortcut in action.shortcutSet.shortcuts) {
          keymap.addShortcut(action.id, shortcut)
        }
      }
    }
  }

  companion object {
    private const val PLUGIN_ID = "com.gitlab.lae.intellij.actions.tree"
  }
}
