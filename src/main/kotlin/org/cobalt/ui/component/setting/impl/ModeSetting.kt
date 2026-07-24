package org.cobalt.ui.component.setting.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import org.cobalt.ui.component.setting.Setting
import org.cobalt.util.input.Mouse
import org.cobalt.util.render.SkiaRenderer

class ModeSetting(
  name: String,
  description: String,
  defaultValue: Int,
  val options: Array<String>,
) : Setting<Int>(name, description, defaultValue) {

  init {
    require(options.isNotEmpty()) {
      "ModeSetting: '$name' must have at least one option"
    }
  }

  override fun read(element: JsonElement) {
    this.value = element.asInt
  }

  override fun write(): JsonElement = JsonPrimitive(value)

  override fun renderSetting() {
    val display = options.getOrNull(value).orEmpty()
    val buttonWidth = SkiaRenderer.textWidth(SkiaRenderer.regularFont, display, FONT_SIZE) + 30f
    val startX = xPos + width - buttonWidth - PADDING
    val startY = yPos + (height - BUTTON_HEIGHT) / 2

    val borderColor = theme.border
    val textColor = theme.textPrimary

    SkiaRenderer.roundedRect(
      startX, startY,
      buttonWidth, BUTTON_HEIGHT,
      5f, theme.backgroundPrimary
    )

    SkiaRenderer.roundedOutline(
      startX, startY,
      buttonWidth, BUTTON_HEIGHT,
      1f, 5f, borderColor
    )

    val textWidth = SkiaRenderer.textWidth(SkiaRenderer.regularFont, display, FONT_SIZE)

    SkiaRenderer.text(
      SkiaRenderer.regularFont, display,
      startX + (buttonWidth - textWidth) / 2,
      startY + (BUTTON_HEIGHT - FONT_SIZE) / 2,
      FONT_SIZE, textColor
    )
  }

  override fun mouseClicked(button: Int): Boolean {
    if (options.isEmpty()) {
      return false
    }

    val display = options.getOrNull(value).orEmpty()
    val buttonWidth = SkiaRenderer.textWidth(SkiaRenderer.regularFont, display, FONT_SIZE) + 30f
    val startX = xPos + width - buttonWidth - PADDING
    val startY = yPos + (height - BUTTON_HEIGHT) / 2

    if (!Mouse.isHoveringOver(startX, startY, buttonWidth, BUTTON_HEIGHT)) {
      return false
    }

    value = when (button) {
      0 -> (value + 1) % options.size
      1 -> (value - 1).mod(options.size)
      else -> return false
    }

    return true
  }

  companion object {
    private const val BUTTON_HEIGHT = 35f
    private const val FONT_SIZE = 12f
  }

}


