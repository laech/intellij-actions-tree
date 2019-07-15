package com.gitlab.lae.intellij.actions.tree.action;

import com.google.gson.stream.JsonWriter;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

public final class ExportIdeActions extends AnAction implements DumbAware {

    @Override
    public void actionPerformed(AnActionEvent e) {

        VirtualFileWrapper result = FileChooserFactory.getInstance()
                .createSaveFileDialog(new FileSaverDescriptor(
                        "Export IDE Actions", ""), (Project) null)
                .save(null, "actions.json");
        if (result == null) {
            return;
        }

        ApplicationManager.getApplication().runWriteAction(() ->
                export(result.getFile(), e.getActionManager()));

        Project project = e.getProject();
        VirtualFile virtualFile = result.getVirtualFile();
        if (project == null || virtualFile == null) {
            return;
        }
        new OpenFileDescriptor(project, virtualFile).navigate(true);
    }

    private void export(File file, ActionManager mgr) {
        try (BufferedWriter fileWriter = Files.newBufferedWriter(file.toPath());
             JsonWriter writer = new JsonWriter(fileWriter)) {

            writer.setIndent("  ");
            writer.beginArray();

            for (String id : mgr.getActionIds("")) {
                AnAction action = mgr.getActionOrStub(id);
                if (action == null) {
                    continue;
                }
                String name = action.getTemplatePresentation().getText();
                if (name == null) {
                    name = "";
                }
                writer.beginObject()
                        .name("id").value(id)
                        .name("name").value(name)
                        .endObject();
            }

            writer.endArray();
            writer.flush();

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
