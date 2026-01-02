package org.cobalt.api.module.setting.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import net.minecraft.client.util.InputUtil
import org.cobalt.Cobalt.mc
import org.cobalt.api.module.setting.Setting
import org.lwjgl.glfw.GLFW

class KeyBindSetting(
  name: String,
  description: String,
  defaultValue: Int,
) : Setting<Int>(name, description, defaultValue) {

  val keyName: String
    get() = when (value) {
      -1 -> "None"
      GLFW.GLFW_KEY_LEFT_SUPER, GLFW.GLFW_KEY_RIGHT_SUPER -> "Super"
      GLFW.GLFW_KEY_LEFT_SHIFT -> "Left Shift"
      GLFW.GLFW_KEY_RIGHT_SHIFT -> "Right Shift"
      GLFW.GLFW_KEY_LEFT_CONTROL -> "Left Control"
      GLFW.GLFW_KEY_RIGHT_CONTROL -> "Right Control"
      GLFW.GLFW_KEY_LEFT_ALT -> "Left Alt"
      GLFW.GLFW_KEY_RIGHT_ALT -> "Right Alt"
      GLFW.GLFW_KEY_SPACE -> "Space"
      GLFW.GLFW_KEY_ENTER -> "Enter"
      GLFW.GLFW_KEY_TAB -> "Tab"
      GLFW.GLFW_KEY_CAPS_LOCK -> "Caps Lock"
      else -> GLFW.glfwGetKeyName(value, 0)?.uppercase() ?: "Unknown"
    }

  fun isPressed(): Boolean {
    if (value == -1) return false
    return InputUtil.isKeyPressed(mc.window, value)
  }

  override fun read(element: JsonElement) {
    this.value = element.asInt
  }

  override fun write(): JsonElement {
    return JsonPrimitive(value)
  }

}
