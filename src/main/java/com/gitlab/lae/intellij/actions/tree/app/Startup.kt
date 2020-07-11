package com.gitlab.lae.intellij.actions.tree.app

import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.project.Project

class Startup : AppLifecycleListener {
  override fun appStarting(projectFromCommandLine: Project?) {
    super.appStarting(projectFromCommandLine)
    App.reload()
  }
}
