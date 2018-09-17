package com.gitlab.lae.intellij.actions.tree

import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications.Bus.notify
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.keymap.Keymap
import com.intellij.openapi.keymap.ex.KeymapManagerEx
import org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace
import java.nio.file.Paths

class AppComponent : ApplicationComponent {

    private val actions = mutableListOf<ActionNode>()

    override fun initComponent() {
        super.initComponent()
        reload(ActionManager.getInstance())
    }

    fun reload(actionManager: ActionManager) {
        val newActions = loadActions()
        val keymaps = KeymapManagerEx.getInstanceEx().allKeymaps

        keymaps.removeShortcuts(actions)
        actionManager.unregisterActions(actions)
        actions.clear()

        actions.addAll(newActions)
        actionManager.registerActions(actions)
        keymaps.setShortcuts(actions)
    }
}

private fun loadActions(): List<ActionNode> {
    val props = PropertiesComponent.getInstance()
    val conf = props.getValue(confKey)?.trim() ?: return emptyList()
    return try {

        parseJsonActions(Paths.get(conf))

    } catch (e: Exception) {
        notify(Notification(
                "ActionsTree",
                "Failed to load keymap",
                "Failed to load keymap: $conf\n${getStackTrace(e)}",
                NotificationType.ERROR
        ))
        emptyList()
    }
}

private fun Array<Keymap>.removeShortcuts(actions: Iterable<ActionNode>) {
    forEach { keymap ->
        actions.forEach {
            keymap.removeAllActionShortcuts(it.id)
        }
    }
}

private fun Array<Keymap>.setShortcuts(actions: Iterable<ActionNode>) {
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
    forEach { keymap ->
        actions.forEach { action ->
            keymap.removeAllActionShortcuts(action.id)
            action.keys.forEach { key ->
                keymap.addShortcut(action.id, KeyboardShortcut(key, null))
            }
        }
    }
}

private fun ActionManager.registerActions(actions: Iterable<ActionNode>) {
    val pluginId = PluginId.getId("com.gitlab.lae.intellij.actions.tree")
    actions.forEach { action ->
        registerAction(action.id, action.toAction(this), pluginId)
    }
}

private fun ActionManager.unregisterActions(actions: Iterable<ActionNode>) =
        actions.asSequence().map(ActionNode::id).forEach(::unregisterAction)

