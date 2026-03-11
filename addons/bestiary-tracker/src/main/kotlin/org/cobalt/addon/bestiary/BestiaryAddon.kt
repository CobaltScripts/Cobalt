package org.cobalt.addon.bestiary

import org.cobalt.api.addon.Addon
import org.cobalt.api.module.Module

class BestiaryAddon : Addon() {

  override fun onLoad() {}

  override fun onUnload() {}

  override fun getModules(): List<Module> = listOf(BestiaryModule())
}
