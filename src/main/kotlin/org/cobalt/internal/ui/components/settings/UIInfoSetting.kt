package org.cobalt.internal.ui.components.settings

import java.awt.Color
import org.cobalt.api.module.setting.impl.InfoSetting
import org.cobalt.api.module.setting.impl.InfoType
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.ui.UIComponent

internal class UIInfoSetting(private val setting: InfoSetting) : UIComponent(
  x = 0F,
  y = 0F,
  width = 627.5F,
  height = 60F,
) {

  private fun getColors(): Triple<Int, Int, Int> {
    return when (setting.type) {
      InfoType.INFO -> Triple(
        Color(61, 94, 149, 25).rgb,
        Color(61, 94, 149, 150).rgb,
        Color(61, 94, 149, 255).rgb
      )
      InfoType.WARNING -> Triple(
        Color(184, 134, 11, 25).rgb,
        Color(184, 134, 11, 150).rgb,
        Color(184, 134, 11, 255).rgb
      )
      InfoType.SUCCESS -> Triple(
        Color(34, 139, 34, 25).rgb,
        Color(34, 139, 34, 150).rgb,
        Color(34, 139, 34, 255).rgb
      )
      InfoType.ERROR -> Triple(
        Color(178, 34, 34, 25).rgb,
        Color(178, 34, 34, 150).rgb,
        Color(178, 34, 34, 255).rgb
      )
    }
  }

  private fun getIcon(): String {
    return when (setting.type) {
      InfoType.INFO -> "/assets/cobalt/icons/settings/info.svg"
      InfoType.WARNING -> "/assets/cobalt/icons/settings/warning.svg"
      InfoType.SUCCESS -> "/assets/cobalt/icons/settings/checkmark.svg"
      InfoType.ERROR -> "/assets/cobalt/icons/settings/error.svg"
    }
  }

  override fun render() {
    val (bgColor, borderColor, iconColor) = getColors()

    NVGRenderer.rect(x, y, width, height, bgColor, 10F)
    NVGRenderer.hollowRect(x, y, width, height, 1.5F, borderColor, 10F)

    val iconSize = 24F
    val iconX = x + 12F
    val iconY = y + (height / 2F) - (iconSize / 2F)

    try {
      val icon = NVGRenderer.createImage(getIcon())
      NVGRenderer.image(icon, iconX, iconY, iconSize, iconSize, colorMask = iconColor)
    } catch (_: Exception) {
    }

    if (setting.name.isNotEmpty()) {
      val titleY = y + (height / 2F) - 14F
      NVGRenderer.text(
        setting.name,
        x + 50F,
        titleY,
        15F,
        Color(230, 230, 230).rgb
      )

      val textY = y + (height / 2F) + 5F
      NVGRenderer.text(
        setting.text,
        x + 50F,
        textY,
        12F,
        Color(179, 179, 179).rgb
      )
    } else {
      val textY = y + (height / 2F) - 6F
      NVGRenderer.text(
        setting.text,
        x + 50F,
        textY,
        13F,
        Color(200, 200, 200).rgb
      )
    }
  }
}
