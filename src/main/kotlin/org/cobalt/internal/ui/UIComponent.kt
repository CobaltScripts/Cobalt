package org.cobalt.internal.ui

import net.minecraft.client.input.CharInput
import net.minecraft.client.input.KeyInput

internal abstract class UIComponent(
  var x: Float,
  var y: Float,
  var width: Float,
  var height: Float,
) {

  abstract fun render()

  open fun mouseClicked(button: Int): Boolean = false
  open fun mouseReleased(button: Int): Boolean = false
  open fun mouseDragged(button: Int, offsetX: Double, offsetY: Double): Boolean = false

  open fun charTyped(input: CharInput): Boolean = false
  open fun keyPressed(input: KeyInput): Boolean = false

  fun updateBounds(x: Float, y: Float): UIComponent {
    this.x = x
    this.y = y
    return this
  }

}
