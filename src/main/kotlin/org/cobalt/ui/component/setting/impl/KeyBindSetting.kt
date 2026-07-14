package org.cobalt.ui.component.setting.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.input.KeyEvent
import org.cobalt.ui.component.setting.Setting
import org.cobalt.util.input.Mouse
import org.cobalt.util.render.SkiaRenderer
import org.lwjgl.glfw.GLFW

class KeyBindSetting(
  name: String,
  description: String,
  defaultValue: InputConstants.Key,
) : Setting<InputConstants.Key>(name, description, defaultValue) {

  constructor(
    name: String,
    description: String,
    defaultKeyCode: Int,
  ) : this(name, description, InputConstants.Type.KEYSYM.getOrCreate(defaultKeyCode))

  override fun read(element: JsonElement) {
    this.value = InputConstants.getKey(element.asString)
  }

  override fun write(): JsonElement {
    return JsonPrimitive(value.name)
  }

  private var listening = false

  private val displayText: String
    get() = if (listening) "..." else value.displayName.string

  private val buttonWidth: Float
    get() = SkiaRenderer.textWidth(SkiaRenderer.regularFont, displayText, FONT_SIZE) + 30f

  override fun renderSetting() {
    val startX = xPos + width - buttonWidth - PADDING
    val startY = yPos + (height - BUTTON_HEIGHT) / 2

    SkiaRenderer.roundedRect(
      startX, startY,
      buttonWidth, BUTTON_HEIGHT,
      5f, theme.backgroundPrimary
    )

    SkiaRenderer.roundedOutline(
      startX, startY,
      buttonWidth, BUTTON_HEIGHT,
      1f, 5f, theme.border
    )

    val textWidth = SkiaRenderer.textWidth(SkiaRenderer.regularFont, displayText, FONT_SIZE)

    SkiaRenderer.text(
      SkiaRenderer.regularFont, displayText,
      startX + (buttonWidth - textWidth) / 2,
      startY + (BUTTON_HEIGHT - FONT_SIZE) / 2,
      FONT_SIZE, theme.textPrimary
    )
  }

  override fun mouseClicked(button: Int): Boolean {
    val startX = xPos + width - buttonWidth - PADDING
    val startY = yPos + (height - BUTTON_HEIGHT) / 2
    val isHovered = Mouse.isHoveringOver(startX, startY, buttonWidth, BUTTON_HEIGHT)

    if (listening) {
      value = InputConstants.Type.MOUSE.getOrCreate(button)
      listening = false
      return true
    } else if (button == 0 && isHovered) {
      listening = true
      return true
    }

    return false
  }

  override fun keyPressed(input: KeyEvent): Boolean {
    if (!listening) {
      return false
    }

    value = when (input.key) {
      GLFW.GLFW_KEY_ESCAPE, GLFW.GLFW_KEY_BACKSPACE -> InputConstants.UNKNOWN
      else -> InputConstants.getKey(input)
    }

    listening = false
    return true
  }

  companion object {
    private const val BUTTON_HEIGHT = 35f
    private const val FONT_SIZE = 12f
  }

}
