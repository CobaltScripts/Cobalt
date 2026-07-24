package org.cobalt.ui.component

import org.cobalt.ui.UIComponent
import org.cobalt.ui.theme.Theme
import org.cobalt.ui.theme.ThemeManager
import org.cobalt.util.color.updateAlpha
import org.cobalt.util.input.Mouse
import org.cobalt.util.render.SkiaRenderer

class ThemeComponent(val newTheme: Theme) : UIComponent(
  width = WIDTH,
  height = HEIGHT,
) {

  override fun renderComponent() {
    SkiaRenderer.roundedRect(
      x = xPos,
      y = yPos,
      width = width,
      height = height,
      radius = 5f,
      color = newTheme.accentPrimary.updateAlpha(40)
    )

    SkiaRenderer.roundedOutline(
      x = xPos,
      y = yPos,
      width = width,
      height = height,
      thickness = 1f,
      radius = 5f,
      color = newTheme.accentPrimary
    )

    val startX = xPos + INNER_PADDING
    var startY = yPos + INNER_PADDING

    SkiaRenderer.text(
      font = SkiaRenderer.regularFont,
      text = newTheme.name,
      x = startX,
      y = startY,
      size = THEME_NAME_FONT_SIZE,
      color = theme.textPrimary
    )

    val isActive = theme == newTheme
    val text = if (isActive) "Active" else "Inactive"
    val textColor = if (isActive) theme.success else theme.textMuted
    val statusY = startY + THEME_NAME_FONT_SIZE + TEXT_PADDING

    SkiaRenderer.text(
      font = SkiaRenderer.regularFont,
      text = text,
      x = startX,
      y = statusY,
      size = STATUS_FONT_SIZE,
      color = textColor
    )

    startY = yPos + height - SWATCH_HEIGHT - INNER_PADDING

    SkiaRenderer.roundedRect(
      x = startX,
      y = startY,
      width = SWATCH_WIDTH,
      height = SWATCH_HEIGHT,
      radius = 2.5f,
      color = newTheme.accentPrimary
    )

    SkiaRenderer.roundedRect(
      x = startX + SWATCH_WIDTH + SWATCH_PADDING,
      y = startY,
      width = SWATCH_WIDTH,
      height = SWATCH_HEIGHT,
      radius = 2.5f,
      color = newTheme.accentSecondary
    )
  }

  override fun mouseClicked(button: Int): Boolean {
    val isHovered = Mouse.isHoveringOver(xPos, yPos, width, height)

    if (button != 0 || !isHovered) {
      return false
    }

    ThemeManager.changeTheme(newTheme)
    return true
  }

  companion object {
    val WIDTH = (TopbarComponent.width - 80) / 3f
    const val HEIGHT = 150f
    private const val INNER_PADDING = 15f
    private const val THEME_NAME_FONT_SIZE = 18f
    private const val TEXT_PADDING = 5f
    private const val STATUS_FONT_SIZE = 12f
    private const val SWATCH_WIDTH = 40f
    private const val SWATCH_HEIGHT = 6f
    private const val SWATCH_PADDING = 10f
  }

}
