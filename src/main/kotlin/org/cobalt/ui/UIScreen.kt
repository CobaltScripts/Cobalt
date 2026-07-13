package org.cobalt.ui

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import org.cobalt.util.render.skia.SkiaPIP

abstract class UIScreen : Screen(Component.empty()) {

  val components = mutableListOf<UIComponent>()

  abstract fun renderSkia()

  override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
    super.extractRenderState(graphics, mouseX, mouseY, a)

    SkiaPIP.drawSkia(graphics) {
      renderSkia()
    }
  }

  override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
    return components.any { it.mouseClicked(event.button()) } ||
      super.mouseClicked(event, doubleClick)
  }

  override fun mouseReleased(event: MouseButtonEvent): Boolean {
    return components.any { it.mouseReleased(event.button()) } ||
      super.mouseReleased(event)
  }

  override fun mouseDragged(event: MouseButtonEvent, dx: Double, dy: Double): Boolean {
    return components.any { it.mouseDragged(event.button(), dx, dy) } ||
      super.mouseDragged(event, dx, dy)
  }

  override fun mouseScrolled(x: Double, y: Double, scrollX: Double, scrollY: Double): Boolean {
    return components.any { it.mouseScrolled(scrollX, scrollY) } ||
      super.mouseScrolled(x, y, scrollX, scrollY)
  }

  override fun charTyped(event: CharacterEvent): Boolean {
    return components.any { it.charTyped(event) } ||
      super.charTyped(event)
  }

  override fun keyPressed(event: KeyEvent): Boolean {
    return components.any { it.keyPressed(event) } ||
      super.keyPressed(event)
  }

  override fun keyReleased(event: KeyEvent): Boolean {
    return components.any { it.keyReleased(event) } ||
      super.keyReleased(event)
  }

  override fun isPauseScreen(): Boolean = false

}
