package com.gitlab.lae.intellij.actions.tree;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.ex.KeymapManagerEx;

import javax.swing.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.gitlab.lae.intellij.actions.tree.Json.parseJsonActions;
import static java.util.Collections.emptyList;

public final class AppComponent implements ApplicationComponent {

    private static final String PLUGIN_ID =
            "com.gitlab.lae.intellij.actions.tree";

    private final List<ActionNode> actions = new ArrayList<>();

    @Override
    public void initComponent() {
        reload(ActionManager.getInstance());
    }

    void reload(ActionManager actionManager) {
        List<ActionNode> newActions = loadActions();
        Keymap[] keymaps = KeymapManagerEx.getInstanceEx().getAllKeymaps();

        removeShortcuts(keymaps, actions);
        unregisterActions(actionManager, actions);
        actions.clear();

        actions.addAll(newActions);
        registerActions(actionManager, actions);
        setShortcuts(keymaps, actions);
    }

    private List<ActionNode> loadActions() {

        String conf = PropertiesComponent.getInstance()
                .getValue(AppConfigurable.CONF_KEY);

        if (conf != null) {
            conf = conf.trim();
        } else {
            return emptyList();
        }

        try {
            return parseJsonActions(Paths.get(conf));
        } catch (Exception e) {
            Notifications.Bus.notify(new Notification(
                    "ActionsTree",
                    "Failed to load keymap",
                    "Failed to load keymap: $conf\n${getStackTrace(e)}",
                    NotificationType.ERROR
            ));
            return emptyList();
        }
    }

    private static void removeShortcuts(
            Keymap[] keymaps,
            Iterable<ActionNode> actions
    ) {
        for (Keymap keymap : keymaps) {
            for (ActionNode action : actions) {
                keymap.removeAllActionShortcuts(action.id());
            }
        }
    }

    private static void setShortcuts(
            Keymap[] keymaps,
            Iterable<ActionNode> actions
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
        for (Keymap keymap : keymaps) {
            for (ActionNode action : actions) {
                keymap.removeAllActionShortcuts(action.id());
                for (KeyStroke key : action.keys()) {
                    Shortcut shortcut = new KeyboardShortcut(key, null);
                    keymap.addShortcut(action.id(), shortcut);
                }
            }
        }
    }

    private static void registerActions(
            ActionManager actionManager,
            Iterable<ActionNode> actions
    ) {
        PluginId pluginId = PluginId.getId(PLUGIN_ID);
        for (ActionNode node : actions) {
            AnAction action = node.toAction(actionManager);
            actionManager.registerAction(node.id(), action, pluginId);
        }
    }

    private static void unregisterActions(
            ActionManager actionManager,
            Iterable<ActionNode> actions
    ) {
        for (ActionNode action : actions) {
            actionManager.unregisterAction(action.id());
        }
    }
}
