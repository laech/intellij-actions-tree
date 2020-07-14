package com.gitlab.lae.intellij.actions.tree.action

import com.google.gson.stream.JsonWriter
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import java.io.File
import kotlin.text.Charsets.UTF_8

class ExportIdeActions : AnAction(), DumbAware {

  override fun actionPerformed(e: AnActionEvent) {
    val descriptor = FileSaverDescriptor("Export IDE Actions", "")
    val result = FileChooserFactory.getInstance()
      .createSaveFileDialog(descriptor, null as Project?)
      .save(null, "actions.json")
      ?: return

    ApplicationManager.getApplication().runWriteAction {
      export(result.file, e.actionManager)
    }

    val project = e.project ?: return
    val virtualFile = result.virtualFile ?: return
    OpenFileDescriptor(project, virtualFile).navigate(true)
  }

  private fun export(file: File, mgr: ActionManager) {
    file.writer(UTF_8).use {
      JsonWriter(it).use { writer ->
        writer.setIndent("  ")
        writer.beginArray()
        mgr.getActionIds("").forEach { id ->
          val action = mgr.getActionOrStub(id) ?: return@forEach
          val name = action.templatePresentation.text ?: ""
          writer.beginObject()
            .name("id").value(id)
            .name("name").value(name)
            .endObject()
        }
        writer.endArray()
        writer.flush()
      }
    }
  }
}
