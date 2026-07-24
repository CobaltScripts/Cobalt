package org.cobalt.ui

import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import org.cobalt.ui.theme.Theme
import org.cobalt.ui.theme.ThemeManager

abstract class UIComponent(
  var xPos: Float = 0f,
  var yPos: Float = 0f,
  open val width: Float = 0.0f,
  open val height: Float = 0.0f,
) {

  var parent: UIComponent? = null

  protected inline val theme: Theme
    get() = ThemeManager.activeTheme

  private val children =
    mutableListOf<UIComponent>()

  abstract fun renderComponent()

  fun addChild(component: UIComponent) {
    require(component != this) { "Cannot add this component as a child" }

    children.add(component)
    component.parent = this
  }

  fun removeAllChildren() {
    children.clear()
  }

  open fun mouseClicked(button: Int): Boolean {
    return children.any { it.mouseClicked(button) }
  }

  open fun mouseReleased(button: Int): Boolean {
    return children.any { it.mouseReleased(button) }
  }

  open fun mouseDragged(button: Int, offsetX: Double, offsetY: Double): Boolean {
    return children.any { it.mouseDragged(button, offsetX, offsetY) }
  }

  open fun mouseScrolled(horizontalAmount: Double, verticalAmount: Double): Boolean {
    return children.any { it.mouseScrolled(horizontalAmount, verticalAmount) }
  }

  open fun charTyped(input: CharacterEvent): Boolean {
    return children.any { it.charTyped(input) }
  }

  open fun keyPressed(input: KeyEvent): Boolean {
    return children.any { it.keyPressed(input) }
  }

  open fun keyReleased(input: KeyEvent): Boolean {
    return children.any { it.keyReleased(input) }
  }

  fun updateBounds(xPos: Float, yPos: Float): UIComponent {
    this.xPos = xPos
    this.yPos = yPos
    return this
  }

}
