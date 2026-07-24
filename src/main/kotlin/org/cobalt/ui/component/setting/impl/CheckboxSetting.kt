package org.cobalt.ui.component.setting.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import org.cobalt.ui.animation.ColorAnimation
import org.cobalt.ui.animation.EaseOutAnimation
import org.cobalt.ui.component.setting.Setting
import org.cobalt.util.color.updateAlpha
import org.cobalt.util.input.Mouse
import org.cobalt.util.render.SkiaRenderer

class CheckboxSetting(
  name: String,
  description: String,
  defaultValue: Boolean,
) : Setting<Boolean>(name, description, defaultValue) {

  private val colorAnimation = ColorAnimation(150L)
  private val alphaAnimation = EaseOutAnimation(150L)

  override fun read(element: JsonElement) {
    this.value = element.asBoolean
  }

  override fun write(): JsonElement {
    return JsonPrimitive(value)
  }

  override fun renderSetting() {
    val startX = xPos + width - PADDING - SIZE
    val startY = yPos + (height - SIZE) / 2

    val alpha = alphaAnimation.get(0f, 40f, !value).toInt()
    val borderColor = colorAnimation.get(theme.border, theme.accentPrimary, !value)
    val bgColor = colorAnimation.get(theme.backgroundPrimary, theme.accentPrimary, !value)

    SkiaRenderer.roundedRect(
      x = startX,
      y = startY,
      width = SIZE,
      height = SIZE,
      radius = 5f,
      color = bgColor.updateAlpha(alpha)
    )

    SkiaRenderer.roundedOutline(
      x = startX,
      y = startY,
      width = SIZE,
      height = SIZE,
      thickness = 1f,
      radius = 5f,
      color = borderColor
    )

    SkiaRenderer.circle(
      startX + SIZE / 2,
      startY + SIZE / 2,
      2f, borderColor
    )
  }

  override fun mouseClicked(button: Int): Boolean {
    if (!Mouse.isHoveringOver(xPos, yPos, width, height)) {
      return false
    }

    value = !value
    colorAnimation.start()
    alphaAnimation.start()

    return true
  }

  companion object {
    private const val SIZE = 22.5f
  }

}
