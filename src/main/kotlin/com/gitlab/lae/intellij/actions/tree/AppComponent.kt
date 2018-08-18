package com.gitlab.lae.intellij.actions.tree

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.keymap.Keymap
import com.intellij.openapi.keymap.KeymapManager
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
        actions.forEach { (id, _) -> manager.unregisterAction(id) }
        actions = loadActions()
        actions.forEach { (id, action) -> manager.registerAction(id, action) }

        val keymaps = KeymapManager.getInstance()
        keymaps.activeKeymap.registerShortcuts(actions)
        keymaps.addKeymapManagerListener({ it.registerShortcuts(actions) }, { })
    }
}

private fun loadActions(): List<GenAction> {
    val props = PropertiesComponent.getInstance()
    val conf = props.getValue(confKey)?.trim() ?: return emptyList()
    return parseJsonActions(Paths.get(conf)).map(::GenAction)
}

private fun Keymap.registerShortcuts(actions: List<GenAction>) {
    actions.forEach { (id, action) ->
        action.key?.also { addShortcut(id, KeyboardShortcut(it, null)) }
    }
}

private data class GenAction(val id: String, val action: ActionNode) {
    constructor(action: ActionNode) : this(
            "ActionsTree ${identityHashCode(action)} (${action.key})",
            action)
}
