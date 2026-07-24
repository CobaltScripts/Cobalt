package org.cobalt.module.impl.visual

import kotlin.math.roundToInt
import org.cobalt.module.ModuleCategory
import org.cobalt.module.type.RenderableModule
import org.cobalt.util.render.SkiaRenderer
import org.cobalt.util.server.ConnectionTracker

object PerformanceHUDModule : RenderableModule(
  name = "PerformanceHUD",
  category = ModuleCategory.VISUAL,
) {

  override val width: Float
    get() {
      var width = PADDING * 2

      for ((index, stat) in getStats().withIndex()) {
        if (index > 0) {
          width += PADDING + 2 * TEXT_SPACING
        }

        width += SkiaRenderer.textWidth(SkiaRenderer.boldFont, stat.value, FONT_SIZE) + TEXT_SPACING
        width += SkiaRenderer.textWidth(SkiaRenderer.boldFont, stat.unit, FONT_SIZE)
      }

      return width
    }

  override val height: Float
    get() = 50f

  override fun renderComponent() {
    SkiaRenderer.roundedRect(
      x = xPos, y = yPos, width = width, height = height,
      radius = 5f, color = theme.backgroundPrimary
    )

    var currentX = xPos + PADDING
    val centerY = yPos + height / 2
    val textY = centerY - FONT_SIZE / 2

    for ((index, stat) in getStats().withIndex()) {
      if (index > 0) {
        val dividerX = currentX + DIVIDER_GAP
        val midY = yPos + height / 2

        SkiaRenderer.line(
          x1 = dividerX, y1 = midY - DIVIDER_HALF_HEIGHT,
          x2 = dividerX, y2 = midY + DIVIDER_HALF_HEIGHT,
          thickness = 2f, color = theme.border
        )

        currentX = dividerX + DIVIDER_GAP
      }

      var textX = currentX

      SkiaRenderer.text(
        font = SkiaRenderer.boldFont,
        text = stat.value,
        x = textX, y = textY,
        size = FONT_SIZE, color = theme.textPrimary
      )

      textX += SkiaRenderer.textWidth(SkiaRenderer.boldFont, stat.value, FONT_SIZE) + TEXT_SPACING

      SkiaRenderer.text(
        font = SkiaRenderer.boldFont,
        text = stat.unit,
        x = textX, y = textY,
        size = FONT_SIZE, color = theme.textDisabled
      )

      currentX = textX + SkiaRenderer.textWidth(SkiaRenderer.boldFont, stat.unit, FONT_SIZE)
    }
  }

  private fun getStats() = listOf(
    Stat(minecraft.fps.toString(), "FPS"),
    Stat(ConnectionTracker.averageTps.roundToInt().toString(), "TPS"),
    Stat(ConnectionTracker.averagePing.toString(), "MS"),
  )

  private const val PADDING = 25f
  private const val FONT_SIZE = 16f
  private const val TEXT_SPACING = 5f
  private const val DIVIDER_HALF_HEIGHT = 10f
  private const val DIVIDER_GAP = PADDING / 2 + TEXT_SPACING

  private data class Stat(val value: String, val unit: String)

}
