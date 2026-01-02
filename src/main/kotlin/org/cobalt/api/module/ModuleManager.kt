package org.cobalt.api.module

object ModuleManager {

  private val moduleList = mutableListOf<Module>()

  fun getModules(): List<Module> {
    return moduleList
  }

  internal fun addModules(modules: List<Module>) {
    moduleList.addAll(modules)
  }

}
