package org.cobalt.ui.component.button

import org.cobalt.ui.UIComponent
import org.cobalt.ui.animation.ColorAnimation
import org.cobalt.ui.animation.EaseOutAnimation
import org.cobalt.util.color.updateAlpha
import org.cobalt.util.input.Mouse
import org.cobalt.util.render.SkiaRenderer

class IconButton(
  resourcePath: String,
  val onClick: () -> Unit,
) : UIComponent(
  width = 40f,
  height = 40f
) {

  private val icon = SkiaRenderer.createImage(resourcePath)
  private val colorAnimation = ColorAnimation(150L)
  private val alphaAnimation = EaseOutAnimation(150L)

  private var wasHovering = false

  override fun renderComponent() {
    val hovering = Mouse.isHoveringOver(xPos, yPos, width, height)

    if (hovering != wasHovering) {
      colorAnimation.start()
      alphaAnimation.start()
      wasHovering = hovering
    }

    val alpha = alphaAnimation.get(0f, 40f, !hovering).toInt()
    val borderColor = colorAnimation.get(theme.border, theme.accentPrimary, !hovering)
    val iconColor = colorAnimation.get(theme.textMuted, theme.accentPrimary, !hovering)

    SkiaRenderer.roundedRect(
      xPos, yPos,
      width, height,
      5f, theme.backgroundPrimary
    )

    SkiaRenderer.roundedRect(
      xPos, yPos,
      width, height,
      5f, theme.accentPrimary.updateAlpha(alpha)
    )

    SkiaRenderer.roundedOutline(
      xPos, yPos,
      width, height,
      1f, 5f, borderColor
    )

    val iconX = xPos + (width - ICON_SIZE) / 2f
    val iconY = yPos + (height - ICON_SIZE) / 2f

    SkiaRenderer.image(
      icon, iconX, iconY, ICON_SIZE, ICON_SIZE,
      color = iconColor
    )
  }

  override fun mouseClicked(button: Int): Boolean {
    if (button != 0 || !Mouse.isHoveringOver(xPos, yPos, width, height)) {
      return false
    }

    onClick()
    return true
  }

  companion object {
    private const val ICON_SIZE = 16F
  }

}
