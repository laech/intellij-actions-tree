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

    private val actions = mutableListOf<ActionNode>()

    override fun initComponent() {
        super.initComponent()
        reload(ActionManager.getInstance())
        KeymapManager.getInstance().addKeymapManagerListener(this, Disposable { })
    }

    fun reload(actionManager: ActionManager) {
        val newActions = loadActions()
        val activeKeymap = KeymapManager.getInstance().activeKeymap

        actions.forEachShortcut(activeKeymap::removeShortcut)
        actionManager.unregisterActions(actions)
        actions.clear()

        actions.addAll(newActions)
        actionManager.registerActions(actions)
        actions.forEachShortcut(activeKeymap::addShortcut)
    }

    override fun activeKeymapChanged(keymap: Keymap) =
            actions.forEachShortcut(keymap::addShortcut)
}

private fun loadActions(): List<ActionNode> {
    val props = PropertiesComponent.getInstance()
    val conf = props.getValue(confKey)?.trim() ?: return emptyList()
    return try {

        parseJsonActions(Paths.get(conf))
                .map { it.copy(name = "Actions Tree User Defined: ${it.name}") }

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

private fun Iterable<ActionNode>.forEachShortcut(
        handler: (String, KeyboardShortcut) -> Unit
) = forEach { action ->
    action.keys.forEach { key ->
        handler(action.id, KeyboardShortcut(key, null))
    }
}

private fun ActionManager.registerActions(actions: Iterable<ActionNode>) =
        actions.forEach { action ->
            action.toAction(this)?.also {
                registerAction(action.id, it)
            }
        }

private fun ActionManager.unregisterActions(actions: Iterable<ActionNode>) =
        actions.asSequence().map(ActionNode::id).forEach(::unregisterAction)

