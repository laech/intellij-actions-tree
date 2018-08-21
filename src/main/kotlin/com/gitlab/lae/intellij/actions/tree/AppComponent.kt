package com.gitlab.lae.intellij.actions.tree

import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.keymap.Keymap
import com.intellij.openapi.keymap.KeymapManager
import org.apache.commons.lang3.exception.ExceptionUtils
import java.lang.System.identityHashCode
import java.nio.file.Paths

class AppComponent : ApplicationComponent {

    @Volatile
    private var actions: List<GenAction> = emptyList()

    override fun initComponent() {
        super.initComponent()
        reload()
    }

    fun reload() {
        val manager = ActionManager.getInstance()
        val newActions = loadActions()
        actions.forEach { (id, _) -> manager.unregisterAction(id) }
        actions = newActions
        actions.forEach { (id, node) ->
            val action = node.toAction(manager)
            if (action != null) {
                manager.registerAction(id, action)
            }
        }

        val keymaps = KeymapManager.getInstance()
        keymaps.activeKeymap.registerShortcuts(actions)
        keymaps.addKeymapManagerListener({ it.registerShortcuts(actions) }, { })
    }
}

private fun loadActions(): List<GenAction> {
    val props = PropertiesComponent.getInstance()
    val conf = props.getValue(confKey)?.trim() ?: return emptyList()
    return try {

        parseJsonActions(Paths.get(conf))
                .filterNot { it is ActionSeparator }
                .map(::GenAction)

    } catch (e: Exception) {
        Notifications.Bus.notify(Notification(
                "ActionsTree",
                "Failed to load keymap",
                "Failed to load keymap: $conf\n${ExceptionUtils.getStackTrace(e)}",
                NotificationType.ERROR
        ))
        emptyList()
    }
}

private fun Keymap.registerShortcuts(actions: List<GenAction>) {
    actions.forEach { (id, action) ->
        action.keys.forEach { key ->
            addShortcut(id, KeyboardShortcut(key, null))
        }
    }
}

private data class GenAction(val id: String, val action: ActionNode) {
    constructor(action: ActionNode) : this(
            "Actions Tree Generated ${identityHashCode(action)} ${action.keys.joinToString(",")}",
            action)
}
