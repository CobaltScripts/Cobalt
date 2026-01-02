package org.cobalt.api.addon

import org.cobalt.api.module.Module

abstract class Addon {

  abstract fun onLoad()
  abstract fun onUnload()

  open fun getModules(): List<Module> =
    emptyList()

}
