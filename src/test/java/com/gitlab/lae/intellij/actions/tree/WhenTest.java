package com.gitlab.lae.intellij.actions.tree;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.ToolWindow;
import org.junit.Test;

import javax.swing.text.JTextComponent;

import static com.intellij.openapi.actionSystem.CommonDataKeys.PROJECT;
import static com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE;
import static com.intellij.openapi.actionSystem.PlatformDataKeys.TOOL_WINDOW;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public final class WhenTest {

    @Test
    public void any() {
        assertFalse(When.any().test(null));
        assertTrue(When.any(When.ALWAYS).test(null));
        assertFalse(When.any(When.NEVER).test(null));
        assertTrue(When.any(When.ALWAYS, When.NEVER).test(null));
        assertTrue(When.any(When.ALWAYS, When.ALWAYS).test(null));
    }

    @Test
    public void all() {
        assertTrue(When.all().test(null));
        assertTrue(When.all(When.ALWAYS).test(null));
        assertFalse(When.all(When.NEVER).test(null));
        assertTrue(When.all(When.ALWAYS, When.ALWAYS).test(null));
        assertFalse(When.all(When.ALWAYS, When.NEVER).test(null));
    }

    @Test
    public void not() {
        assertFalse(When.not(When.ALWAYS).test(null));
        assertTrue(When.not(When.not(When.ALWAYS)).test(null));
    }

    @Test
    public void fileExtension() {
        var file = mock(VirtualFile.class);
        var context = mock(DataContext.class);
        when(context.getData(VIRTUAL_FILE)).thenReturn(file);

        var when = When.fileExtension("txt");
        when(file.getExtension()).thenReturn("txt");
        assertTrue(when.test(context));

        when(file.getExtension()).thenReturn("jpg");
        assertFalse(when.test(context));
    }

    @Test
    public void toolWindowActive() {
        var toolWindow = mock(ToolWindow.class);
        var context = mock(DataContext.class);
        when(context.getData(TOOL_WINDOW)).thenReturn(toolWindow);

        var when = When.toolWindowActive("Project");
        when(toolWindow.getStripeTitle()).thenReturn("Project");
        when(toolWindow.isActive()).thenReturn(false);
        assertFalse(when.test(context));

        when(toolWindow.isActive()).thenReturn(true);
        assertTrue(when.test(context));

        when(toolWindow.getStripeTitle()).thenReturn("Test");
        assertFalse(when.test(context));
    }

    @Test
    public void toolWindowTabActive() {
        var toolWindow = mock(ToolWindow.class);
        var context = mock(DataContext.class);
        when(context.getData(TOOL_WINDOW)).thenReturn(toolWindow);

        var when = When.toolWindowTabActive("Project");
        when(toolWindow.getTitle()).thenReturn("Project");
        when(toolWindow.isActive()).thenReturn(false);
        assertFalse(when.test(context));

        when(toolWindow.isActive()).thenReturn(true);
        assertTrue(when.test(context));

        when(toolWindow.getTitle()).thenReturn("Test");
        assertFalse(when.test(context));
    }

    @Test
    public void inputFocused() {

        var inputFocused = When.INPUT_FOCUSED;
        var component = mock(JTextComponent.class);

        var focusManager = mock(IdeFocusManager.class);
        when(focusManager.getFocusOwner()).thenReturn(component);

        var project = mock(Project.class);
        when(project.isInitialized()).thenReturn(true);
        when(project.getComponent(IdeFocusManager.class)).thenReturn(focusManager);

        var context = mock(DataContext.class);
        when(context.getData(PROJECT.getName())).thenReturn(project);

        inputFocused.test(context);
        verify(context).getData(PROJECT.getName());
        verify(project).getComponent(IdeFocusManager.class);
        verify(focusManager).getFocusOwner();
        verify(component).isEditable();

        when(component.isEditable()).thenReturn(true);
        assertTrue(inputFocused.test(context));

        when(component.isEditable()).thenReturn(false);
        assertFalse(inputFocused.test(context));

        when(focusManager.getFocusOwner()).thenReturn(null);
        assertFalse(inputFocused.test(context));
    }
}
