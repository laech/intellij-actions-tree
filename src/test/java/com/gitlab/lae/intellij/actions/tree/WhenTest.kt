package com.gitlab.lae.intellij.actions.tree

import com.gitlab.lae.intellij.actions.tree.When.ALWAYS
import com.gitlab.lae.intellij.actions.tree.When.NEVER
import com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys.TOOL_WINDOW
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WhenTest {

  @Test
  fun any() {
    assertFalse(When.any().test(null))
    assertTrue(When.any(ALWAYS).test(null))
    assertFalse(When.any(NEVER).test(null))
    assertTrue(When.any(ALWAYS, NEVER).test(null))
    assertTrue(When.any(ALWAYS, ALWAYS).test(null))
  }

  @Test
  fun all() {
    assertTrue(When.all().test(null))
    assertTrue(When.all(ALWAYS).test(null))
    assertFalse(When.all(NEVER).test(null))
    assertTrue(When.all(ALWAYS, ALWAYS).test(null))
    assertFalse(When.all(ALWAYS, NEVER).test(null))
  }

  @Test
  fun not() {
    assertFalse(When.not(ALWAYS).test(null))
    assertTrue(When.not(When.not(ALWAYS)).test(null))
  }

  @Test
  fun fileExtension() {
    val file = mock<VirtualFile>()
    val context = mock<DataContext>()
    whenever(context.getData(VIRTUAL_FILE)).thenReturn(file)

    val condition = When.fileExtension("txt")
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

    val condtion = When.toolWindowActive("Project")
    whenever(toolWindow.stripeTitle).thenReturn("Project")
    whenever(toolWindow.isActive).thenReturn(false)
    assertFalse(condtion.test(context))

    whenever(toolWindow.isActive).thenReturn(true)
    assertTrue(condtion.test(context))

    whenever(toolWindow.stripeTitle).thenReturn("Test")
    assertFalse(condtion.test(context))
  }

  @Test
  fun toolWindowTabActive() {
    val toolWindow = mock<ToolWindow>()
    val context = mock<DataContext>()
    whenever(context.getData(TOOL_WINDOW)).thenReturn(toolWindow)

    val condition = When.toolWindowTabActive("Project")
    whenever(toolWindow.title).thenReturn("Project")
    whenever(toolWindow.isActive).thenReturn(false)
    assertFalse(condition.test(context))

    whenever(toolWindow.isActive).thenReturn(true)
    assertTrue(condition.test(context))

    whenever(toolWindow.title).thenReturn("Test")
    assertFalse(condition.test(context))
  }
}
