package org.cobalt.ui.component

import net.fabricmc.loader.api.FabricLoader
import org.cobalt.Cobalt.minecraft
import org.cobalt.module.ModuleCategory
import org.cobalt.module.impl.misc.NickHider
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
      x = xPos,
      y = yPos,
      width = width,
      height = height,
      radius = 10f,
      color = theme.backgroundSecondary,
      corners = SkiaCorner.LEFT_SIDE
    )

    val titleTextWidth = SkiaRenderer.textWidth(SkiaRenderer.boldFont, TITLE_TEXT, TITLE_FONT_SIZE)
    val titleTextX = xPos + (width - titleTextWidth) / 2
    val titleTextY = yPos + TITLE_PADDING

    SkiaRenderer.text(
      font = SkiaRenderer.boldFont,
      text = TITLE_TEXT,
      x = titleTextX,
      y = titleTextY,
      size = TITLE_FONT_SIZE,
      color = theme.textPrimary
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
      x = boxX,
      y = boxY,
      width = USER_INFO_WIDTH,
      height = USER_INFO_HEIGHT,
      radius = USER_INFO_CORNER_RADIUS,
      color = theme.backgroundPrimary,
    )

    SkiaRenderer.roundedOutline(
      x = boxX,
      y = boxY,
      width = USER_INFO_WIDTH,
      height = USER_INFO_HEIGHT,
      thickness = 1f,
      radius = USER_INFO_CORNER_RADIUS,
      color = theme.border
    )

    val playerFaceX = boxX + USER_INFO_INNER_PADDING
    val playerFaceY = boxY + (USER_INFO_HEIGHT - PLAYER_FACE_SIDE_LENGTH) / 2

    SkiaRenderer.image(
      image = playerFace ?: steveFace,
      x = playerFaceX,
      y = playerFaceY,
      width = PLAYER_FACE_SIDE_LENGTH,
      height = PLAYER_FACE_SIDE_LENGTH,
      radius = PLAYER_FACE_SIDE_LENGTH / 2
    )

    SkiaRenderer.roundedOutline(
      x = playerFaceX,
      y = playerFaceY,
      width = PLAYER_FACE_SIDE_LENGTH,
      height = PLAYER_FACE_SIDE_LENGTH,
      thickness = 1f,
      radius = PLAYER_FACE_SIDE_LENGTH / 2,
      color = theme.border
    )

    val textX = boxX + PLAYER_FACE_SIDE_LENGTH + (USER_INFO_INNER_PADDING * 2)
    val textY = boxY + USER_INFO_INNER_PADDING

    SkiaRenderer.text(
      font = SkiaRenderer.regularFont,
      text = if (NickHider.enabled && !NickHider.nickname.isEmpty()) NickHider.nickname else minecraft.gameProfile.name,
      x = textX, y = textY,
      size = USER_INFO_TEXT_SIZE,
      color = theme.textPrimary
    )

    val role = if (FabricLoader.getInstance().isDevelopmentEnvironment) "Dev" else "User"

    SkiaRenderer.text(
      font = SkiaRenderer.regularFont,
      text = role,
      x = textX,
      y = textY + USER_INFO_TEXT_SIZE + 2f,
      size = USER_INFO_TEXT_SIZE,
      color = theme.textSecondary,
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
