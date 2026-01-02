package org.cobalt.internal.test

import org.cobalt.api.addon.Addon
import org.cobalt.api.addon.AddonMetadata
import org.cobalt.api.module.Module
import org.cobalt.api.module.setting.impl.InfoSetting
import org.cobalt.api.module.setting.impl.InfoType
//REMOVE THIS SOMETIME TO RMOVE TEST ADDON!!!!
@Suppress("unused")
class TestModule : Module(
  name = "Test Settings"
) {

  init {
    addSetting(
      InfoSetting(
        name = "Information",
        text = "This is an informational message",
        type = InfoType.INFO
      )
    )

    addSetting(
      InfoSetting(
        name = "Warning",
        text = "This is a warning message",
        type = InfoType.WARNING
      )
    )

    addSetting(
      InfoSetting(
        name = "Success",
        text = "This is a success message",
        type = InfoType.SUCCESS
      )
    )

    addSetting(
      InfoSetting(
        name = "Error",
        text = "This is an error message",
        type = InfoType.ERROR
      )
    )

    addSetting(
      InfoSetting(
        name = null,
        text = "This info has no title, just a message",
        type = InfoType.INFO
      )
    )
  }
}

internal class TestAddon : Addon() {
  override fun onLoad() {}
  override fun onUnload() {}

  override fun getModules() = listOf(TestModule())
}

