package com.gitlab.lae.intellij.actions.tree

import com.intellij.icons.AllIcons
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.fields.ExtendableTextField
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

const val confKey = "com.gitlab.lae.intellij.actions.tree.conf"

class AppConfigurable : Configurable {

    private val settings by lazy {
        PropertiesComponent.getInstance()
    }

    private lateinit var panel: JPanel
    private lateinit var confLocation: ExtendableTextField

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
        confLocation = ExtendableTextField()
        confLocation.addExtension(object : ExtendableTextField.Extension {

            override fun getIcon(hovered: Boolean) =
                    if (hovered) AllIcons.General.OpenDiskHover
                    else AllIcons.General.OpenDisk

            override fun getActionOnClick() = Runnable {
                val file = FileChooser.chooseFile(FileChooserDescriptor(
                        true, false, false, false, false, false), null, null)
                if (file != null) {
                    confLocation.text = file.path
                }
            }
        })

        val row = JPanel(BorderLayout())
        row.add(JLabel("Configuration File: "), BorderLayout.LINE_START)
        row.add(confLocation, BorderLayout.CENTER)

        panel = JPanel(BorderLayout())
        panel.add(row, BorderLayout.PAGE_START)
        return panel
    }

}
