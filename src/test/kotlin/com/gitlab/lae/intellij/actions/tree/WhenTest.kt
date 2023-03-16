package com.gitlab.lae.intellij.actions.tree

import com.gitlab.lae.intellij.actions.tree.When.All
import com.gitlab.lae.intellij.actions.tree.When.Always
import com.gitlab.lae.intellij.actions.tree.When.Any
import com.gitlab.lae.intellij.actions.tree.When.FileExtension
import com.gitlab.lae.intellij.actions.tree.When.Never
import com.gitlab.lae.intellij.actions.tree.When.Not
import com.gitlab.lae.intellij.actions.tree.When.PathExists
import com.gitlab.lae.intellij.actions.tree.When.ToolWindowActive
import com.gitlab.lae.intellij.actions.tree.When.ToolWindowTabActive
import com.intellij.openapi.actionSystem.CommonDataKeys.PROJECT
import com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys.TOOL_WINDOW
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import java.nio.file.Paths
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class WhenTest {

  @Test
  fun any() {
    assertFalse(Any().test(mock()))
    assertTrue(Any(Always).test(mock()))
    assertFalse(Any(Never).test(mock()))
    assertTrue(Any(Always, Never).test(mock()))
    assertTrue(Any(Always, Always).test(mock()))
  }

  @Test
  fun all() {
    assertTrue(All().test(mock()))
    assertTrue(All(Always).test(mock()))
    assertFalse(All(Never).test(mock()))
    assertTrue(All(Always, Always).test(mock()))
    assertFalse(All(Always, Never).test(mock()))
  }

  @Test
  fun not() {
    assertFalse(Not(Always).test(mock()))
    assertTrue(Not(Not(Always)).test(mock()))
  }

  @Test
  fun fileExtension() {
    val file = mock<VirtualFile>()
    val context = mock<DataContext>()
    whenever(context.getData(VIRTUAL_FILE)).thenReturn(file)

    val condition = FileExtension("txt")
    whenever(file.extension).thenReturn("txt")
    assertTrue(condition.test(context))

    whenever(file.extension).thenReturn("jpg")
    assertFalse(condition.test(context))
  }

  @Test
  fun toolWindowActive() {
    val toolWindow = mock<ToolWindow>()
    val context = mock<DataContext>()
    whenever(context.getData(TOOL_WINDOW)).thenReturn(toolWindow)

    val condition = ToolWindowActive("Project")
    whenever(toolWindow.stripeTitle).thenReturn("Project")
    whenever(toolWindow.isActive).thenReturn(false)
    assertFalse(condition.test(context))

    whenever(toolWindow.isActive).thenReturn(true)
    assertTrue(condition.test(context))

    whenever(toolWindow.stripeTitle).thenReturn("Test")
    assertFalse(condition.test(context))
  }

  @Test
  fun toolWindowTabActive() {
    val toolWindow = mock<ToolWindow>()
    val context = mock<DataContext>()
    whenever(context.getData(TOOL_WINDOW)).thenReturn(toolWindow)

    val condition = ToolWindowTabActive("Project")
    whenever(toolWindow.title).thenReturn("Project")
    whenever(toolWindow.isActive).thenReturn(false)
    assertFalse(condition.test(context))

    whenever(toolWindow.isActive).thenReturn(true)
    assertTrue(condition.test(context))

    whenever(toolWindow.title).thenReturn("Test")
    assertFalse(condition.test(context))
  }

  @Test
  fun pathExists() {
    val context = mock<DataContext>()
    assertTrue(PathExists(Paths.get("").toAbsolutePath().toString()).test(context))
    assertFalse(PathExists("/adsfxdfr").test(context))
    assertFalse(PathExists("adsfxdfr").test(context))

    val folder = TemporaryFolder()
    try {
      folder.create()
      folder.newFile("test")
      val project = mock<Project>()
      whenever(project.basePath).thenReturn(folder.root.path)
      whenever(context.getData(PROJECT)).thenReturn(project)
      assertTrue(PathExists("test").test(context))
    } finally {
      folder.delete()
    }
  }
}
