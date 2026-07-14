package org.cobalt.ui.component

import org.cobalt.Cobalt.minecraft
import org.cobalt.module.ModuleCategory
import org.cobalt.ui.UIComponent
import org.cobalt.ui.component.button.SidebarButton
import org.cobalt.util.render.SkiaRenderer
import org.cobalt.util.render.skia.data.SkiaCorner
import org.cobalt.util.render.skia.data.SkiaImage
import org.cobalt.util.scheduling.Multithreading

object SidebarComponent : UIComponent(
  width = 250f,
  height = 800f
) {

  private val buttons = mutableListOf<SidebarButton>()
  private val steveFace = SkiaRenderer.createImage("/assets/cobalt/ui/steve.png")
  private var playerFace: SkiaImage? = null

  init {
    for (category in ModuleCategory.entries) {
      val button = SidebarButton(category)
      this.addChild(button)
      buttons.add(button)
    }

    Multithreading.runAsync {
      playerFace = SkiaRenderer.createImage("https://mc-heads.net/avatar/${minecraft.user.name}/100/face.png")
    }
  }

  override fun renderComponent() {
    SkiaRenderer.roundedRect(
      xPos, yPos, width, height,
      10f, theme.backgroundSecondary,
      SkiaCorner.LEFT_SIDE
    )

    val titleTextWidth = SkiaRenderer.textWidth(SkiaRenderer.boldFont, TITLE_TEXT, TITLE_FONT_SIZE)
    val titleTextX = xPos + (width - titleTextWidth) / 2
    val titleTextY = yPos + TITLE_PADDING

    SkiaRenderer.text(
      SkiaRenderer.boldFont, TITLE_TEXT,
      titleTextX, titleTextY,
      TITLE_FONT_SIZE, theme.textPrimary
    )

    val buttonX = xPos + (width - SidebarButton.WIDTH) / 2f
    var buttonY = yPos + TITLE_FONT_SIZE + (TITLE_PADDING * 2)

    buttons.forEach { button ->
      button
        .updateBounds(buttonX, buttonY)
        .renderComponent()

      buttonY += SidebarButton.HEIGHT + BUTTONS_SPACING
    }

    drawUserInfo()
  }

  private fun drawUserInfo() {
    val boxX = xPos + USER_INFO_OUTER_PADDING
    val boxY = yPos + height - (USER_INFO_HEIGHT + USER_INFO_OUTER_PADDING)

    SkiaRenderer.roundedRect(
      boxX, boxY, USER_INFO_WIDTH, USER_INFO_HEIGHT,
      USER_INFO_CORNER_RADIUS, theme.backgroundPrimary,
    )

    SkiaRenderer.roundedOutline(
      boxX, boxY, USER_INFO_WIDTH, USER_INFO_HEIGHT,
      1f, USER_INFO_CORNER_RADIUS, theme.border
    )

    val playerFaceX = boxX + USER_INFO_INNER_PADDING
    val playerFaceY = boxY + (USER_INFO_HEIGHT - PLAYER_FACE_SIDE_LENGTH) / 2

    SkiaRenderer.image(
      playerFace ?: steveFace,
      playerFaceX, playerFaceY,
      PLAYER_FACE_SIDE_LENGTH, PLAYER_FACE_SIDE_LENGTH,
      PLAYER_FACE_SIDE_LENGTH / 2
    )

    SkiaRenderer.roundedOutline(
      playerFaceX, playerFaceY,
      PLAYER_FACE_SIDE_LENGTH, PLAYER_FACE_SIDE_LENGTH,
      1f, PLAYER_FACE_SIDE_LENGTH / 2, theme.border
    )

    val textX = boxX + PLAYER_FACE_SIDE_LENGTH + (USER_INFO_INNER_PADDING * 2)
    val textY = boxY + USER_INFO_INNER_PADDING

    SkiaRenderer.text(
      SkiaRenderer.regularFont, minecraft.gameProfile.name,
      textX, textY, USER_INFO_TEXT_SIZE, theme.textPrimary
    )

    SkiaRenderer.text(
      SkiaRenderer.regularFont, "User",
      textX, textY + USER_INFO_TEXT_SIZE + 2f,
      USER_INFO_TEXT_SIZE, theme.textSecondary,
    )
  }

  private const val TITLE_TEXT = "cobalt"
  private const val TITLE_FONT_SIZE = 28f
  private const val TITLE_PADDING = 50f
  private const val BUTTONS_SPACING = 5f
  private const val USER_INFO_OUTER_PADDING = 15f
  private const val USER_INFO_INNER_PADDING = 12f
  private const val USER_INFO_CORNER_RADIUS = 5f
  private const val USER_INFO_TEXT_SIZE = 12.5f
  private const val USER_INFO_HEIGHT = 55f
  private const val PLAYER_FACE_SIDE_LENGTH = USER_INFO_HEIGHT - (USER_INFO_INNER_PADDING * 2)

  private val USER_INFO_WIDTH = width - (USER_INFO_OUTER_PADDING * 2)

}
