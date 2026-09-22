package org.cobalt.ui.component.button

import org.cobalt.ui.UIComponent
import org.cobalt.ui.animation.HoverFade
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
  private val hoverFade = HoverFade()

  override fun renderComponent() {
    val hovering = Mouse.isHoveringOver(xPos, yPos, width, height)
    hoverFade.update(hovering)

    val alpha = hoverFade.overlayAlpha(hovering)
    val borderColor = hoverFade.color(theme.border, theme.accentPrimary, hovering)
    val iconColor = hoverFade.color(theme.textMuted, theme.accentPrimary, hovering)

    SkiaRenderer.roundedRect(
      x = xPos,
      y = yPos,
      width = width,
      height = height,
      radius = 5f,
      color = theme.backgroundPrimary
    )

    SkiaRenderer.roundedRect(
      x = xPos,
      y = yPos,
      width = width,
      height = height,
      radius = 5f,
      color = theme.accentPrimary.updateAlpha(alpha)
    )

    SkiaRenderer.roundedOutline(
      x = xPos,
      y = yPos,
      width = width,
      height = height,
      thickness = 1f,
      radius = 5f,
      color = borderColor
    )

    val iconX = xPos + (width - ICON_SIZE) / 2f
    val iconY = yPos + (height - ICON_SIZE) / 2f

    SkiaRenderer.image(
      image = icon,
      x = iconX,
      y = iconY,
      width = ICON_SIZE,
      height = ICON_SIZE,
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
