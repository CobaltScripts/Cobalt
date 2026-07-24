package org.cobalt.ui.component.setting.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import org.cobalt.ui.animation.ColorAnimation
import org.cobalt.ui.animation.EaseOutAnimation
import org.cobalt.ui.component.setting.Setting
import org.cobalt.util.color.updateAlpha
import org.cobalt.util.input.Mouse
import org.cobalt.util.render.SkiaRenderer

class ButtonSetting(
  name: String,
  description: String,
  val buttonLabel: String,
  val onClick: Runnable,
) : Setting<String>(name, description, "") {

  override fun read(element: JsonElement) = Unit
  override fun write(): JsonElement = JsonPrimitive("")

  private val colorAnim = ColorAnimation(150L)
  private val alphaAnim = EaseOutAnimation(150L)
  private var wasHovering = false

  private val buttonWidth =
    SkiaRenderer.textWidth(SkiaRenderer.regularFont, buttonLabel, FONT_SIZE) + 30f

  override fun renderSetting() {
    val buttonWidth = buttonWidth
    val startX = xPos + width - buttonWidth - PADDING
    val startY = yPos + (height - BUTTON_HEIGHT) / 2

    val hovering = Mouse.isHoveringOver(startX, startY, buttonWidth, BUTTON_HEIGHT)

    if (hovering != wasHovering) {
      colorAnim.start()
      alphaAnim.start()
      wasHovering = hovering
    }

    val alpha = alphaAnim.get(0f, 40f, !hovering).toInt()
    val borderColor = colorAnim.get(theme.border, theme.accentPrimary, !hovering)
    val textColor = colorAnim.get(theme.textPrimary, theme.accentPrimary, !hovering)

    SkiaRenderer.roundedRect(
      x = startX,
      y = startY,
      width = buttonWidth,
      height = BUTTON_HEIGHT,
      radius = 5f,
      color = theme.backgroundPrimary
    )

    SkiaRenderer.roundedRect(
      x = startX,
      y = startY,
      width = buttonWidth,
      height = BUTTON_HEIGHT,
      radius = 5f,
      color = theme.accentPrimary.updateAlpha(alpha)
    )

    SkiaRenderer.roundedOutline(
      x = startX,
      y = startY,
      width = buttonWidth,
      height = BUTTON_HEIGHT,
      thickness = 1f,
      radius = 5f,
      color = borderColor
    )

    val textWidth = SkiaRenderer.textWidth(SkiaRenderer.regularFont, buttonLabel, FONT_SIZE)

    SkiaRenderer.text(
      font = SkiaRenderer.regularFont,
      text = buttonLabel,
      x = startX + (buttonWidth - textWidth) / 2,
      y = startY + (BUTTON_HEIGHT - FONT_SIZE) / 2,
      size = FONT_SIZE,
      color = textColor
    )
  }

  override fun mouseClicked(button: Int): Boolean {
    val startX = xPos + width - buttonWidth - PADDING
    val startY = yPos + (height - BUTTON_HEIGHT) / 2

    if (button == 0 && Mouse.isHoveringOver(startX, startY, buttonWidth, BUTTON_HEIGHT)) {
      onClick.run()
      return true
    }

    return false
  }

  companion object {
    private const val BUTTON_HEIGHT = 35f
    private const val FONT_SIZE = 12f
  }

}
