package com.gitlab.lae.intellij.actions.tree.app;

import com.gitlab.lae.intellij.actions.tree.ActionNode;
import com.intellij.ide.DataManager;
import com.intellij.ide.IdeEventQueue;
import com.intellij.ide.IdePopupManager;
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
import com.intellij.openapi.ui.popup.JBPopupFactory;

import javax.swing.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.gitlab.lae.intellij.actions.tree.json.ActionNodeParser.parseJsonActions;
import static java.util.Collections.emptyList;

public final class AppComponent implements ApplicationComponent {

    private static final String PLUGIN_ID =
            "com.gitlab.lae.intellij.actions.tree";

    private final List<ActionNode> actions = new ArrayList<>();

    @Override
    public void initComponent() {
        reload();
    }

    public void reload() {
        reload(
                ActionManager.getInstance(),
                IdeEventQueue.getInstance().getPopupManager(),
                JBPopupFactory.getInstance(),
                DataManager.getInstance(),
                KeymapManagerEx.getInstanceEx(),
                PropertiesComponent.getInstance()
        );
    }

    private void reload(
            ActionManager actionManager,
            IdePopupManager popupManager,
            JBPopupFactory popupFactory,
            DataManager dataManager,
            KeymapManagerEx keymapManager,
            PropertiesComponent properties
    ) {
        List<ActionNode> newActions = loadActions(properties);
        Keymap[] keymaps = keymapManager.getAllKeymaps();

        removeShortcuts(keymaps, actions);
        unregisterActions(actionManager, actions);
        actions.clear();

        actions.addAll(newActions);
        registerActions(
                actionManager,
                popupManager,
                popupFactory,
                dataManager,
                actions
        );
        setShortcuts(keymaps, actions);
    }

    private List<ActionNode> loadActions(PropertiesComponent properties) {

        String conf = properties.getValue(AppConfigurable.CONF_KEY);

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

    private static String wrappedId(ActionNode action) {
        return "ActionsTree." + action.id();
    }

    private static void removeShortcuts(
            Keymap[] keymaps,
            Iterable<ActionNode> actions
    ) {
        for (Keymap keymap : keymaps) {
            for (ActionNode action : actions) {
                keymap.removeAllActionShortcuts(wrappedId(action));
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
                keymap.removeAllActionShortcuts(wrappedId(action));
                for (KeyStroke key : action.keys()) {
                    Shortcut shortcut = new KeyboardShortcut(key, null);
                    keymap.addShortcut(wrappedId(action), shortcut);
                }
            }
        }
    }

    private static void registerActions(
            ActionManager actionManager,
            IdePopupManager popupManager,
            JBPopupFactory popupFactory,
            DataManager dataManager,
            Iterable<ActionNode> actions
    ) {
        PluginId pluginId = PluginId.getId(PLUGIN_ID);
        for (ActionNode node : actions) {
            AnAction action = node.toAction(
                    actionManager,
                    popupManager,
                    popupFactory,
                    dataManager,
                    true
            );
            actionManager.registerAction(wrappedId(node), action, pluginId);
        }
    }

    private static void unregisterActions(
            ActionManager actionManager,
            Iterable<ActionNode> actions
    ) {
        for (ActionNode action : actions) {
            actionManager.unregisterAction(wrappedId(action));
        }
    }
}
