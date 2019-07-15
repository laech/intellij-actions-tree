package com.gitlab.lae.intellij.actions.tree.app;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.awt.*;

public final class AppConfigurable implements Configurable {

    static final String CONF_KEY = "com.gitlab.lae.intellij.actions.tree.conf";

    private JTextField confLocation;

    private PropertiesComponent settings() {
        return PropertiesComponent.getInstance();
    }

    @Override
    public boolean isModified() {
        return isModified(confLocation, settings().getValue(CONF_KEY, ""));
    }

    @Override
    public String getDisplayName() {
        return "Actions Tree";
    }

    @Override
    public void apply() {
        settings().setValue(CONF_KEY, confLocation.getText().trim());
        ApplicationManager
                .getApplication()
                .getComponent(AppComponent.class)
                .reload(ActionManager.getInstance());
    }

    @Override
    public void reset() {
        confLocation.setText(settings().getValue(CONF_KEY, ""));
    }

    @Override
    public JComponent createComponent() {
        confLocation = new JTextField();

        JPanel row = new JPanel(new BorderLayout());
        row.add(new JLabel("Configuration File: "), BorderLayout.LINE_START);
        row.add(new TextFieldWithBrowseButton(confLocation, __ -> {
            VirtualFile file = FileChooser.chooseFile(
                    new FileChooserDescriptor(
                            true, false, false, false, false, false),
                    null, null);
            if (file != null) {
                confLocation.setText(file.getPath());
            }
        }), BorderLayout.CENTER);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(row, BorderLayout.PAGE_START);
        return panel;
    }

}
