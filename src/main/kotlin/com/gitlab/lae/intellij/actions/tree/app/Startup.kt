package com.gitlab.lae.intellij.actions.tree.app

import com.intellij.ide.AppLifecycleListener

class Startup : AppLifecycleListener {
  override fun appFrameCreated(commandLineArgs: MutableList<String>) {
    super.appFrameCreated(commandLineArgs)
    App.reload()
  }
}
