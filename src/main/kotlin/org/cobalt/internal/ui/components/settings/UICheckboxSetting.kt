package org.cobalt.internal.ui.components.settings

import java.awt.Color
import org.cobalt.api.module.setting.impl.CheckboxSetting
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.ui.UIComponent
import org.cobalt.internal.ui.animation.ColorAnimation
import org.cobalt.internal.ui.util.isHoveringOver

internal class UICheckboxSetting(private val setting: CheckboxSetting) : UIComponent(
  x = 0F,
  y = 0F,
  width = 627.5F,
  height = 60F,
) {

  private var colorAnim = ColorAnimation(150L)

  override fun render() {
    NVGRenderer.rect(x, y, width, height, Color(42, 42, 42, 50).rgb, 10F)
    NVGRenderer.hollowRect(x, y, width, height, 1F, Color(42, 42, 42).rgb, 10F)

    NVGRenderer.text(
      setting.name,
      x + 20F,
      y + (height / 2F) - 15.5F,
      15F,
      Color(230, 230, 230).rgb
    )

    NVGRenderer.text(
      setting.description,
      x + 20F,
      y + (height / 2F) + 2F,
      12F, Color(179, 179, 179).rgb
    )

    NVGRenderer.rect(
      x + width - 45F,
      y + (height / 2F) - 12.5F,
      25F, 25F,
      colorAnim.get(
        Color(42, 42, 42, 50),
        Color(61, 94, 149, 50),
        !setting.value
      ).rgb, 5F
    )

    NVGRenderer.hollowRect(
      x + width - 45F,
      y + (height / 2F) - 12.5F,
      25F, 25F, 1.5F,
      colorAnim.get(
        Color(42, 42, 42),
        Color(61, 94, 149),
        !setting.value
      ).rgb, 5F
    )

    if (setting.value) {
      NVGRenderer.image(
        checkmarkIcon,
        x + width - 42.5F,
        y + (height / 2F) - 10F,
        20F,
        20F,
        colorMask = colorAnim.get(
          Color(0, 0, 0, 0),
          Color(61, 94, 149),
          !setting.value
        ).rgb
      )
    }
  }

  override fun mouseClicked(button: Int): Boolean {
    if (isHoveringOver(x + width - 45F, y + (height / 2F) - 12.5F, 25F, 25F)) {
      setting.value = !setting.value
      colorAnim.start()
      return true
    }

    return false
  }

  companion object {
    val checkmarkIcon = NVGRenderer.createImage("/assets/cobalt/icons/settings/checkmark.svg")
  }

}
