package org.cobalt.ui.component

import org.cobalt.module.Module
import org.cobalt.ui.UIComponent
import org.cobalt.ui.animation.ColorAnimation
import org.cobalt.ui.animation.EaseOutAnimation
import org.cobalt.util.input.Mouse
import org.cobalt.util.render.SkiaRenderer

class SwitchComponent(val module: Module) : UIComponent(
  width = 30f,
  height = 10f
) {

  private val colorAnimation = ColorAnimation(150L)
  private val xOffsetAnimation = EaseOutAnimation(200L)

  override fun renderComponent() {
    val mainColor = colorAnimation.get(theme.backgroundPrimary, theme.accentPrimary, !module.enabled)
    val knobX = xOffsetAnimation.get(xPos + 1f, xPos + width - KNOB_SIZE - 1f, !module.enabled)

    SkiaRenderer.roundedRect(
      x = xPos,
      y = yPos,
      width = width,
      height = height,
      radius = 5f,
      color = mainColor
    )

    SkiaRenderer.circle(
      x = knobX + KNOB_SIZE / 2f,
      y = yPos + height / 2f,
      radius = KNOB_SIZE / 2f,
      color = theme.textPrimary
    )
  }

  override fun mouseClicked(button: Int): Boolean {
    if (button == 0 && Mouse.isHoveringOver(xPos, yPos, width, height)) {
      module.enabled = !module.enabled
      colorAnimation.start()
      xOffsetAnimation.start()
      return true
    }

    return false
  }

  companion object {
    private const val KNOB_SIZE = 12.5f
  }

}
