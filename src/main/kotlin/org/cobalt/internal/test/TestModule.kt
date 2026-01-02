package org.cobalt.internal.test

import java.awt.Color
import org.cobalt.api.addon.Addon
import org.cobalt.api.addon.AddonMetadata
import org.cobalt.api.module.Module
import org.cobalt.api.module.setting.impl.ColorSetting
import org.cobalt.api.module.setting.impl.InfoSetting
import org.cobalt.api.module.setting.impl.InfoType

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

    addSetting(
      ColorSetting(
        name = "Rohan Likes Children",
        description = "Rohan is a serial child molester and this is his favorite color",
        defaultValue = Color(61, 94, 149, 255).rgb
      )
    )

    addSetting(
      ColorSetting(
        name = "Secondary Color",
        description = "Secondary accent color",
        defaultValue = Color(100, 150, 200, 200).rgb
      )
    )

    addSetting(
      ColorSetting(
        name = "Background Color",
        description = "UI Background color",
        defaultValue = Color(25, 25, 25, 255).rgb
      )
    )
  }
}

internal class TestAddon : Addon() {
  override fun onLoad() {}
  override fun onUnload() {}

  override fun getModules() = listOf(TestModule())
}

