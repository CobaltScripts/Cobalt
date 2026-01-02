package org.cobalt.api.module

import org.cobalt.api.module.setting.Setting

abstract class Module(val name: String) {

  private val settingsList = mutableListOf<Setting<*>>()

  fun addSetting(vararg settings: Setting<*>) {
    settingsList.addAll(listOf(*settings))
  }

  fun getSettings(): List<Setting<*>> {
    return settingsList
  }

}
