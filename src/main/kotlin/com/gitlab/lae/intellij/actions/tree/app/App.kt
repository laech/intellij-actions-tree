package com.gitlab.lae.intellij.actions.tree.app

import com.gitlab.lae.intellij.actions.tree.ActionNode
import com.gitlab.lae.intellij.actions.tree.json.parseJsonActions
import com.google.common.base.Throwables.getStackTraceAsString
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.keymap.Keymap
import com.intellij.openapi.keymap.ex.KeymapManagerEx
import java.nio.file.Paths

object App {

  private const val PLUGIN_ID = "com.gitlab.lae.intellij.actions.tree"

  private val actionIds = HashSet<String>()

  fun reload() {
    reload(
        ActionManager.getInstance(),
        KeymapManagerEx.getInstanceEx(),
        PropertiesComponent.getInstance(),
    )
  }

  private fun reload(
      actionManager: ActionManager,
      keymapManager: KeymapManagerEx,
      properties: PropertiesComponent,
  ) {
    val actions =
        RootAction.merge(
            loadActions(properties),
            actionManager,
        )

    val keymaps = keymapManager.allKeymaps
    for (keymap in keymaps) {
      actionIds.forEach(keymap::removeAllActionShortcuts)
    }

    actionIds.forEach(actionManager::unregisterAction)
    actionIds.clear()

    val pluginId = PluginId.getId(PLUGIN_ID)
    for (action in actions) {
      actionIds.add(action.id)
      actionManager.registerAction(action.id, action, pluginId)
    }

    setShortcuts(keymaps, actions)
  }

  private fun loadActions(properties: PropertiesComponent): List<ActionNode> {
    val conf = properties.getValue(AppConfigurable.CONF_KEY)?.trim() ?: return emptyList()

    return try {
      parseJsonActions(Paths.get(conf))
    } catch (e: Exception) {
      val groupId = "ActionsTree"
      val title = "Failed to load keymap"
      val content = "Failed to load keymap: ${conf}\n${getStackTraceAsString(e)}"
      Notifications.Bus.notify(
          Notification(groupId, title, content, NotificationType.ERROR),
      )
      emptyList()
    }
  }

  private fun setShortcuts(
      keymaps: Array<Keymap>,
      actions: Iterable<RootAction>,
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
        for (keyStroke in action.keyStrokes) {
          keymap.addShortcut(action.id, KeyboardShortcut(keyStroke, null))
        }
      }
    }
  }
}
