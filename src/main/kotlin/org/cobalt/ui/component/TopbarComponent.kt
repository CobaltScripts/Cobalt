package org.cobalt.ui.component

import org.cobalt.Cobalt.minecraft
import org.cobalt.ui.UIComponent
import org.cobalt.ui.component.button.IconButton
import org.cobalt.ui.page.impl.ScriptsPage
import org.cobalt.ui.page.impl.ThemesPage
import org.cobalt.ui.screen.ConfigScreen
import org.cobalt.ui.screen.HudEditorScreen
import org.cobalt.util.render.SkiaRenderer
import org.cobalt.util.render.skia.data.SkiaCorner
import org.cobalt.util.scheduling.TickScheduler

object TopbarComponent : UIComponent() {

  override val width: Float
    get() = 1000f

  override val height: Float
    get() = 70f

  private val searchBar = TextInputComponent(
    width = SEARCHBAR_WIDTH,
    height = SEARCHBAR_HEIGHT,
    fontSize = 14f,
    placeholder = "Search",
    onChange = { query ->
      ConfigScreen.currentPage.onSearchQueryChanged(query)
    }
  )

  private val actionButtons = listOf(
    IconButton("/assets/cobalt/ui/hud.svg") {
      TickScheduler.schedule(1L) { minecraft.gui.setScreen(HudEditorScreen) }
    },

    IconButton("/assets/cobalt/ui/themes.svg") {
      ConfigScreen.currentPage = ThemesPage
    },

    IconButton("/assets/cobalt/ui/scripts.svg") {
      ConfigScreen.currentPage = ScriptsPage
    }
  )

  init {
    addChild(searchBar)
    actionButtons.forEach(::addChild)
  }

  override fun renderComponent() {
    val currentPage = ConfigScreen.currentPage

    SkiaRenderer.roundedRect(
      xPos, yPos,
      width, height,
      10f, theme.backgroundSecondary,
      arrayOf(SkiaCorner.TOP_RIGHT)
    )

    val textX = xPos + INNER_PADDING + 10f
    val textY = yPos + (height - CURRENT_PAGE_TITLE_FONT) / 2

    SkiaRenderer.text(
      SkiaRenderer.regularFont, currentPage.title,
      textX, textY, CURRENT_PAGE_TITLE_FONT, theme.textPrimary
    )

    val searchBarX = xPos + width - SEARCHBAR_WIDTH - INNER_PADDING
    val searchBarY = yPos + (height - SEARCHBAR_HEIGHT) / 2

    searchBar
      .updateBounds(searchBarX, searchBarY)
      .renderComponent()

    var currentXAnchor = searchBarX

    actionButtons.forEach { button ->
      val buttonX = currentXAnchor - BUTTON_SPACING - button.width
      val buttonY = yPos + (height - button.height) / 2f

      button
        .updateBounds(buttonX, buttonY)
        .renderComponent()

      currentXAnchor = buttonX
    }
  }

  private const val SEARCHBAR_WIDTH = 250f
  private const val SEARCHBAR_HEIGHT = 40f
  private const val CURRENT_PAGE_TITLE_FONT = 19f
  private const val INNER_PADDING = 20f
  private const val BUTTON_SPACING = 10f

}
