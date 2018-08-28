package com.gitlab.lae.intellij.actions.tree

import com.google.gson.stream.JsonWriter
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.DumbAware
import java.io.File

class ExportIdeActions : AnAction(), DumbAware {

    override fun actionPerformed(e: AnActionEvent) {
        val result = FileChooserFactory.getInstance()
                .createSaveFileDialog(FileSaverDescriptor("Export IDE Actions", ""), null)
                .save(null, "actions.json") ?: return

        runWriteAction {
            export(result.file, e.actionManager)
        }

        val project = e.project ?: return
        val virtualFile = result.virtualFile ?: return
        OpenFileDescriptor(project, virtualFile).navigate(true)
    }

    private fun export(file: File, mgr: ActionManager) {
        file.writer().use { bob ->
            val writer = JsonWriter(bob)
            writer.setIndent("  ")
            writer.beginArray()
            mgr.getActionIds("").asSequence()
                    .map { id ->
                        val name = mgr.getActionOrStub(id)?.templatePresentation?.text
                        Pair(id, name ?: "")
                    }
                    .sortedBy(Pair<String, String>::first)
                    .forEach { (id, name) ->
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
