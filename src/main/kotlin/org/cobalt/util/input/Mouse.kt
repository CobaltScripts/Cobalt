package org.cobalt.util.input

import net.minecraft.client.input.MouseButtonInfo
import org.cobalt.Cobalt.minecraft
import org.cobalt.Cobalt.runOnClientThread
import org.cobalt.mixin.client.MouseHandlerAccessor
import org.lwjgl.glfw.GLFW

enum class MouseButton {
  LEFT, RIGHT, MIDDLE
}

enum class MouseAction {
  PRESS, RELEASE
}

enum class MouseMode {
  DEFAULT, UNGRAB_MOUSE, LOCK_MOUSE
}

object Mouse {

  @JvmStatic
  var mouseMode: MouseMode = MouseMode.DEFAULT

  @JvmStatic
  val mouseX: Float
    get() = minecraft.mouseHandler.xpos().toFloat()

  @JvmStatic
  val mouseY: Float
    get() = minecraft.mouseHandler.ypos().toFloat()

  @JvmStatic
  fun leftClick() {
    click(GLFW.GLFW_MOUSE_BUTTON_LEFT)
  }

  @JvmStatic
  fun middleClick() {
    click(GLFW.GLFW_MOUSE_BUTTON_MIDDLE)
  }

  @JvmStatic
  fun rightClick() {
    click(GLFW.GLFW_MOUSE_BUTTON_RIGHT)
  }

  @JvmStatic
  fun isHoveringOver(x: Float, y: Float, width: Float, height: Float): Boolean {
    return mouseX >= x &&
      mouseX <= x + width &&
      mouseY >= y &&
      mouseY <= y + height
  }

  private fun click(button: Int) {
    val window = minecraft.window.handle()
    val mouse = minecraft.mouseHandler as MouseHandlerAccessor
    val info = MouseButtonInfo(button, 0)

    runOnClientThread {
      mouse.invokeOnButton(window, info, GLFW.GLFW_PRESS)
      mouse.invokeOnButton(window, info, GLFW.GLFW_RELEASE)
    }
  }

}
