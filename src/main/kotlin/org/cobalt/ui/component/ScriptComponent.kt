package org.cobalt.ui.component

import org.cobalt.module.type.Script
import org.cobalt.ui.UIComponent
import org.cobalt.ui.animation.ColorAnimation
import org.cobalt.ui.animation.EaseOutAnimation
import org.cobalt.ui.screen.ConfigScreen
import org.cobalt.util.color.updateAlpha
import org.cobalt.util.input.Mouse
import org.cobalt.util.render.SkiaRenderer
import org.cobalt.util.render.skia.data.SkiaImage
import org.cobalt.util.scheduling.TickScheduler

class ScriptComponent(val script: Script) : UIComponent(
  width = WIDTH,
  height = 200f
) {

  private val colorAnim = ColorAnimation(200L)
  private val alphaAnim = EaseOutAnimation(200L)

  private val backgroundPicture: SkiaImage? = if (script.backgroundResourcePath.isNotBlank()) {
    SkiaRenderer.createImage(script.backgroundResourcePath)
  } else {
    null
  }

  override fun renderComponent() {
    drawBackgroundPicture()

    val borderColor = colorAnim.get(theme.border, theme.textMuted, !script.enabled)
    val alpha = alphaAnim.get(0f, 255f, !script.enabled).toInt()

    SkiaRenderer.roundedRect(
      x = xPos,
      y = yPos,
      width = width,
      height = height,
      radius = 5f,
      color = theme.backgroundPrimary.updateAlpha(100)
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

    if (script.enabled) {
      SkiaRenderer.image(
        image = pauseIcon,
        x = xPos + (width - ICON_SIZE) / 2,
        y = yPos + (height - ICON_SIZE) / 2,
        width = ICON_SIZE,
        height = ICON_SIZE,
        color = theme.textMuted.updateAlpha(alpha)
      )
    }

    SkiaRenderer.text(
      font = SkiaRenderer.regularFont,
      text = script.name,
      x = xPos + PADDING,
      y = yPos + height - PADDING - FONT_SIZE,
      size = FONT_SIZE,
      color = theme.textPrimary
    )
  }

  private fun drawBackgroundPicture() {
    val picture = backgroundPicture ?: return
    val (imageWidth, imageHeight) = SkiaRenderer.imageSize(picture)
    val scale = maxOf(width / imageWidth, height / imageHeight)
    val drawWidth = imageWidth * scale
    val drawHeight = imageHeight * scale
    val drawX = xPos + (width - drawWidth) / 2f
    val drawY = yPos + (height - drawHeight) / 2f

    SkiaRenderer.pushScissor(
      x = xPos,
      y = yPos,
      width = width,
      height = height,
      radius = 5f
    )

    SkiaRenderer.blurredImage(
      image = picture,
      x = drawX,
      y = drawY,
      width = drawWidth,
      height = drawHeight,
      radius = 2f,
      cornerRadius = 5f
    )

    SkiaRenderer.popScissor()
  }

  override fun mouseClicked(button: Int): Boolean {
    if (button == 0 && Mouse.isHoveringOver(xPos, yPos, width, height)) {
      colorAnim.start()

      if (script.enabled) {
        script.stopScript()
      } else {
        script.startScript()
        TickScheduler.schedule(2L) { ConfigScreen.closeScreen() }
      }

      return true
    }

    return false
  }

  companion object {
    val WIDTH = (TopbarComponent.width - 60) / 2f
    val pauseIcon = SkiaRenderer.createImage("/assets/cobalt/ui/pause.svg")

    private const val PADDING = 20f
    private const val FONT_SIZE = 20f
    private const val ICON_SIZE = 30f
  }

}
