package org.cobalt.ui.page.impl

import kotlin.math.ceil
import org.cobalt.ui.animation.EaseOutAnimation
import org.cobalt.ui.component.ThemeComponent
import org.cobalt.ui.component.button.IconButton
import org.cobalt.ui.helper.ScrollHelper
import org.cobalt.ui.page.Page
import org.cobalt.ui.theme.ThemeManager
import org.cobalt.util.input.Mouse
import org.cobalt.util.render.SkiaRenderer

object ThemesPage : Page() {

  private val themeComponents = mutableListOf<ThemeComponent>()
  private val scrollHelper = ScrollHelper()
  private val openingOffset = EaseOutAnimation(200L)

  override val title: String
    get() = "Themes"

  val reloadButton = IconButton("/assets/cobalt/ui/reload.svg") {
    scrollHelper.reset()
    ThemeManager.reloadThemes()
    initializePage()
  }

  override fun initializePage() {
    openingOffset.start()
    resetComponents()
  }

  override fun onSearchQueryChanged(query: String) {
    resetComponents(query)
  }

  private fun resetComponents(query: String = "") {
    themeComponents.clear()
    scrollHelper.reset()

    removeAllChildren()
    addChild(reloadButton)

    ThemeManager.themes.values
      .filter { theme ->
        query.isBlank() || theme.name.contains(query, ignoreCase = true)
      }
      .forEach { theme ->
        val component = ThemeComponent(theme)

        addChild(component)
        themeComponents.add(component)
      }
  }

  override fun renderComponent() {
    super.renderComponent()

    val pageOffset = openingOffset.get(-30f, 0f)

    val rows = ceil(themeComponents.size.toDouble() / COLUMNS).toFloat()
    val contentHeight = pageOffset + PADDING * 2 + rows * ThemeComponent.HEIGHT + (rows - 1) * SPACING

    scrollHelper.updateMaxScroll(contentHeight, height)
    SkiaRenderer.pushScissor(xPos, yPos, width, height)

    themeComponents.forEachIndexed { index, component ->
      val col = index % COLUMNS
      val row = index / COLUMNS

      val x = xPos + PADDING + col * (ThemeComponent.WIDTH + SPACING)
      val y = yPos + PADDING + pageOffset + row * (ThemeComponent.HEIGHT + SPACING) - scrollHelper.scrollOffset

      component
        .updateBounds(x, y)
        .renderComponent()
    }

    SkiaRenderer.popScissor()

    val buttonX = xPos + width - PADDING - reloadButton.width
    val buttonY = yPos + height - PADDING - reloadButton.height

    reloadButton
      .updateBounds(buttonX, buttonY)
      .renderComponent()
  }

  override fun mouseScrolled(horizontalAmount: Double, verticalAmount: Double): Boolean {
    if (Mouse.isHoveringOver(xPos, yPos, width, height)) {
      scrollHelper.scroll(verticalAmount)
      return true
    }

    return false
  }

  private const val PADDING = 20f
  private const val COLUMNS = 3
  private const val SPACING = 20f

}
