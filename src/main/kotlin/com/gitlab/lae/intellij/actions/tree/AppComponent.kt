package com.gitlab.lae.intellij.actions.tree

import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications.Bus.notify
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.keymap.Keymap
import com.intellij.openapi.keymap.KeymapManager
import com.intellij.openapi.keymap.KeymapManagerListener
import org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace
import java.nio.file.Paths

class AppComponent : ApplicationComponent, KeymapManagerListener {

    private val actions = mutableListOf<GenAction>()

    override fun initComponent() {
        super.initComponent()
        reload(ActionManager.getInstance())
    }

    fun reload(actionManager: ActionManager) {
        val newActions = loadActions()
        val keymapManager = KeymapManager.getInstance()

        keymapManager.activeKeymap.removeShortcuts(actions)
        actionManager.unregisterActions(actions)
        actions.clear()

        actions.addAll(newActions)
        actionManager.registerActions(actions)
        keymapManager.activeKeymap.addShortcuts(actions)
        keymapManager.addKeymapManagerListener(this, Disposable { })
    }

    override fun activeKeymapChanged(keymap: Keymap) =
            keymap.addShortcuts(actions)
}

private fun loadActions(): List<GenAction> {
    val props = PropertiesComponent.getInstance()
    val conf = props.getValue(confKey)?.trim() ?: return emptyList()
    return try {

        parseJsonActions(Paths.get(conf)).mapIndexed(::GenAction)

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

private fun Keymap.addShortcuts(actions: Iterable<GenAction>) =
        actions.forEach { (id, action) ->
            action.keys.forEach { key ->
                addShortcut(id, KeyboardShortcut(key, null))
            }
        }

private fun Keymap.removeShortcuts(actions: Iterable<GenAction>) =
        actions.forEach { (id, action) ->
            action.keys.forEach { key ->
                removeShortcut(id, KeyboardShortcut(key, null))
            }
        }

private fun ActionManager.registerActions(actions: Iterable<GenAction>) =
        actions.forEach { (id, node) ->
            node.toAction(this)?.also { registerAction(id, it) }
        }

private fun ActionManager.unregisterActions(actions: Iterable<GenAction>) =
        actions.forEach { (id, _) -> unregisterAction(id) }

data class GenAction(val id: String, val action: ActionNode) {
    constructor(id: Int, action: ActionNode) : this(
            "Actions Tree Generated #$id ${action.keys.joinToString(",")}",
            action)
}
