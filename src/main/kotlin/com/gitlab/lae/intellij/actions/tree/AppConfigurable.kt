package com.gitlab.lae.intellij.actions.tree

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

const val confKey = "com.gitlab.lae.intellij.actions.tree.conf"

class AppConfigurable : Configurable {

    private val settings by lazy {
        PropertiesComponent.getInstance()
    }

    private lateinit var panel: JPanel
    private lateinit var confLocation: JTextField

    override fun isModified() = isModified(
            confLocation, settings.getValue(confKey, ""))

    override fun getDisplayName() = "Actions Tree"

    override fun apply() {
        settings.setValue(confKey, confLocation.text.trim())
        ApplicationManager
                .getApplication()
                .getComponent(AppComponent::class.java)
                .reload(ActionManager.getInstance())
    }

    override fun reset() {
        confLocation.text = settings.getValue(confKey, "")
    }

    override fun createComponent(): JComponent {
        confLocation = JTextField()

        val row = JPanel(BorderLayout())
        row.add(JLabel("Configuration File: "), BorderLayout.LINE_START)
        row.add(TextFieldWithBrowseButton(confLocation) {
            val file = FileChooser.chooseFile(FileChooserDescriptor(
                    true, false, false, false, false, false), null, null)
            if (file != null) {
                confLocation.text = file.path
            }
        }, BorderLayout.CENTER)

        panel = JPanel(BorderLayout())
        panel.add(row, BorderLayout.PAGE_START)
        return panel
    }

}
