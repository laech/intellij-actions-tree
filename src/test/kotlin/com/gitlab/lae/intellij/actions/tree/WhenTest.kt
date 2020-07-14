package com.gitlab.lae.intellij.actions.tree

import com.gitlab.lae.intellij.actions.tree.When.Companion.ALWAYS
import com.gitlab.lae.intellij.actions.tree.When.Companion.NEVER
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
    assertFalse(When.any().test(mock()))
    assertTrue(When.any(ALWAYS).test(mock()))
    assertFalse(When.any(NEVER).test(mock()))
    assertTrue(When.any(ALWAYS, NEVER).test(mock()))
    assertTrue(When.any(ALWAYS, ALWAYS).test(mock()))
  }

  @Test
  fun all() {
    assertTrue(When.all().test(mock()))
    assertTrue(When.all(ALWAYS).test(mock()))
    assertFalse(When.all(NEVER).test(mock()))
    assertTrue(When.all(ALWAYS, ALWAYS).test(mock()))
    assertFalse(When.all(ALWAYS, NEVER).test(mock()))
  }

  @Test
  fun not() {
    assertFalse(When.not(ALWAYS).test(mock()))
    assertTrue(When.not(When.not(ALWAYS)).test(mock()))
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
