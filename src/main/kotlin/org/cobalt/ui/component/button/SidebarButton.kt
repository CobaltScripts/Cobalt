package org.cobalt.ui.component.button

import org.cobalt.module.ModuleCategory
import org.cobalt.ui.UIComponent
import org.cobalt.ui.animation.ColorAnimation
import org.cobalt.ui.animation.EaseOutAnimation
import org.cobalt.ui.page.impl.ModulesPage
import org.cobalt.ui.screen.ConfigScreen
import org.cobalt.util.color.updateAlpha
import org.cobalt.util.input.Mouse
import org.cobalt.util.render.SkiaRenderer

class SidebarButton(val category: ModuleCategory) : UIComponent(
  width = WIDTH,
  height = HEIGHT
) {

  private val icon = SkiaRenderer.createImage(category.iconPath)
  private val colorAnimation = ColorAnimation(150L)
  private val xOffsetAnimation = EaseOutAnimation(200L)

  private var wasSelected = false
  private val selected: Boolean
    get() = ConfigScreen.selectedCategory == category && ConfigScreen.currentPage == ModulesPage

  override fun renderComponent() {
    val isSelectedNow = selected

    if (wasSelected != isSelectedNow) {
      colorAnimation.start()
      xOffsetAnimation.start()
      wasSelected = isSelectedNow
    }

    val opaqueColor = colorAnimation.get(theme.transparent, theme.accentPrimary.updateAlpha(50), !isSelectedNow)
    val mainColor = colorAnimation.get(theme.transparent, theme.accentPrimary, !isSelectedNow)
    val textColor = colorAnimation.get(theme.textPrimary, theme.accentPrimary, !isSelectedNow)
    val xOffset = xOffsetAnimation.get(0F, TEXT_SELECTED_OFFSET, !isSelectedNow)

    if (isSelectedNow) {
      SkiaRenderer.roundedRect(
        xPos, yPos,
        width, height,
        CORNER_RADIUS,
        opaqueColor
      )

      SkiaRenderer.roundedOutline(
        xPos, yPos,
        width, height,
        1f, CORNER_RADIUS, mainColor
      )
    }

    val iconX = xPos + ICON_LEFT_PADDING
    fun iconY(iconSize: Float) = yPos + (height - iconSize) / 2F

    SkiaRenderer.image(
      icon,
      iconX + xOffset, iconY(ICON_SIZE),
      ICON_SIZE, ICON_SIZE,
      color = textColor
    )

    if (isSelectedNow) {
      SkiaRenderer.image(
        selectedIcon,
        iconX, iconY(SELECTION_ICON_SIZE),
        SELECTION_ICON_SIZE, SELECTION_ICON_SIZE,
        color = mainColor
      )
    }

    val textX = iconX + ICON_SIZE + ICON_TEXT_PADDING
    val textY = yPos + height / 2F - FONT_SIZE / 2F

    SkiaRenderer.text(
      SkiaRenderer.regularFont,
      category.displayName,
      textX + xOffset, textY,
      FONT_SIZE, textColor
    )
  }

  override fun mouseClicked(button: Int): Boolean {
    if (button == 0 && Mouse.isHoveringOver(xPos, yPos, width, height)) {
      ConfigScreen.currentPage = ModulesPage
      ConfigScreen.selectedCategory = category
      return true
    }

    return false
  }

  companion object {
    const val WIDTH = 220f
    const val HEIGHT = 45f

    private const val CORNER_RADIUS = 5f
    private const val FONT_SIZE = 13f
    private const val ICON_SIZE = 15f
    private const val ICON_TEXT_PADDING = 6f
    private const val ICON_LEFT_PADDING = 10f
    private const val SELECTION_ICON_SIZE = 13f
    private const val TEXT_SELECTED_OFFSET = 17f

    private val selectedIcon = SkiaRenderer.createImage("/assets/cobalt/ui/selected.svg")
  }

}
