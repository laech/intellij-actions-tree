package com.gitlab.lae.intellij.actions.tree

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys.TOOL_WINDOW
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class WhenTest {

  @Test
  fun any() {
    assertFalse(When.any().test(null))
    assertTrue(When.any(When.ALWAYS).test(null))
    assertFalse(When.any(When.NEVER).test(null))
    assertTrue(When.any(When.ALWAYS, When.NEVER).test(null))
    assertTrue(When.any(When.ALWAYS, When.ALWAYS).test(null))
  }

  @Test
  fun all() {
    assertTrue(When.all().test(null))
    assertTrue(When.all(When.ALWAYS).test(null))
    assertFalse(When.all(When.NEVER).test(null))
    assertTrue(When.all(When.ALWAYS, When.ALWAYS).test(null))
    assertFalse(When.all(When.ALWAYS, When.NEVER).test(null))
  }

  @Test
  fun not() {
    assertFalse(When.not(When.ALWAYS).test(null))
    assertTrue(When.not(When.not(When.ALWAYS)).test(null))
  }

  @Test
  fun fileExtension() {
    val file = mock(VirtualFile::class.java)
    val context = mock(DataContext::class.java)
    `when`(context.getData(CommonDataKeys.VIRTUAL_FILE)).thenReturn(file)

    val condition = When.fileExtension("txt")
    `when`(file.extension).thenReturn("txt")
    assertTrue(condition.test(context))

    `when`(file.extension).thenReturn("jpg")
    assertFalse(condition.test(context))
  }

  @Test
  fun toolWindowActive() {
    val toolWindow = mock(ToolWindow::class.java)
    val context = mock(DataContext::class.java)
    `when`(context.getData(TOOL_WINDOW)).thenReturn(toolWindow)

    val condtion = When.toolWindowActive("Project")
    `when`(toolWindow.stripeTitle).thenReturn("Project")
    `when`(toolWindow.isActive).thenReturn(false)
    assertFalse(condtion.test(context))

    `when`(toolWindow.isActive).thenReturn(true)
    assertTrue(condtion.test(context))

    `when`(toolWindow.stripeTitle).thenReturn("Test")
    assertFalse(condtion.test(context))
  }

  @Test
  fun toolWindowTabActive() {
    val toolWindow = mock(ToolWindow::class.java)
    val context = mock(DataContext::class.java)
    `when`(context.getData(TOOL_WINDOW)).thenReturn(toolWindow)

    val condition = When.toolWindowTabActive("Project")
    `when`(toolWindow.title).thenReturn("Project")
    `when`(toolWindow.isActive).thenReturn(false)
    assertFalse(condition.test(context))

    `when`(toolWindow.isActive).thenReturn(true)
    assertTrue(condition.test(context))

    `when`(toolWindow.title).thenReturn("Test")
    assertFalse(condition.test(context))
  }
}
