package com.gitlab.lae.intellij.actions.tree.app

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class AppConfigurable : Configurable {

  private lateinit var confLocation: JTextField

  private fun settings() = PropertiesComponent.getInstance()

  override fun isModified() =
    isModified(confLocation, settings().getValue(CONF_KEY, ""))

  override fun getDisplayName() = "Actions Tree"

  override fun apply() {
    settings().setValue(CONF_KEY, confLocation.text.trim())
    App.reload()
  }

  override fun reset() {
    confLocation.text = settings().getValue(CONF_KEY, "")
  }

  override fun createComponent(): JComponent? {
    confLocation = JTextField()

    val row = JPanel(BorderLayout())
    row.add(JLabel("Configuration File: "), BorderLayout.LINE_START)
    row.add(
      TextFieldWithBrowseButton(confLocation) {
        val file = FileChooser.chooseFile(
          FileChooserDescriptor(true, false, false, false, false, false),
          null,
          null,
        )
        if (file != null) {
          confLocation.text = file.path
        }
      },
      BorderLayout.CENTER,
    )

    val panel = JPanel(BorderLayout())
    panel.add(row, BorderLayout.PAGE_START)
    return panel
  }

  companion object {
    const val CONF_KEY = "com.gitlab.lae.intellij.actions.tree.conf"
  }
}
