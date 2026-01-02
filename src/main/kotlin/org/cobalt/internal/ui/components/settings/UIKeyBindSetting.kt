package org.cobalt.internal.ui.components.settings

import java.awt.Color
import net.minecraft.client.input.KeyInput
import net.minecraft.client.util.InputUtil
import org.cobalt.api.module.setting.impl.KeyBindSetting
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.ui.UIComponent
import org.cobalt.internal.ui.util.isHoveringOver
import org.lwjgl.glfw.GLFW

internal class UIKeyBindSetting(private val setting: KeyBindSetting) : UIComponent(
  x = 0F,
  y = 0F,
  width = 627.5F,
  height = 60F,
) {

  private var isListening = false

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
      12F,
      Color(179, 179, 179).rgb
    )

    val text = if (isListening) "Listening..." else setting.keyName.uppercase()
    val textWidth = NVGRenderer.textWidth(text, 15F)

    NVGRenderer.rect(
      x + width - textWidth - 40F, y + (height / 2F) - 12.5F,
      textWidth + 20F, 25F, Color(42, 42, 42, 50).rgb, 5F
    )

    NVGRenderer.hollowRect(
      x + width - textWidth - 40F, y + (height / 2F) - 12.5F,
      textWidth + 20F, 25F, 1F, Color(42, 42, 42).rgb, 5F
    )

    NVGRenderer.text(
      text,
      x + width - textWidth - 30F,
      y + (height / 2F) - 7.5F,
      15F,
      if (isListening) Color(42, 42, 42).rgb else Color(230, 230, 230).rgb
    )
  }

  override fun mouseClicked(button: Int): Boolean {
    val text = if (isListening) "Listening..." else setting.keyName.uppercase()
    val textWidth = NVGRenderer.textWidth(text, 15F)

    if (isListening) {
      setting.value = button
      isListening = false
      return true
    } else if (isHoveringOver(x + width - textWidth - 40F, y + (height / 2F) - 12.5F, textWidth + 20F, 25F) && button == 0) {
      isListening = true
      return true
    }

    return false
  }

  override fun keyPressed(input: KeyInput): Boolean {
    if (!isListening) {
      return false
    }

    val keyCode = InputUtil.fromKeyCode(input).code

    setting.value = when (keyCode) {
      GLFW.GLFW_KEY_ESCAPE, GLFW.GLFW_KEY_BACKSPACE -> -1
      GLFW.GLFW_KEY_ENTER -> setting.value
      else -> keyCode
    }

    isListening = false
    return true
  }

}
