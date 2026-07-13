package org.cobalt.ui.component.setting.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import org.cobalt.ui.component.setting.Setting
import org.cobalt.util.color.updateAlpha
import org.cobalt.util.render.SkiaRenderer

class InfoSetting(
  val text: String,
  val type: Type = Type.INFO,
) : Setting<String>(text, "", "") {

  override fun read(element: JsonElement) = Unit
  override fun write(): JsonElement = JsonPrimitive("")

  override fun renderSetting() {
    SkiaRenderer.roundedRect(
      xPos + INNER_PADDING, yPos + INNER_PADDING,
      width - (INNER_PADDING * 2), height - (INNER_PADDING * 2),
      5f, color.updateAlpha(40)
    )

    SkiaRenderer.roundedOutline(
      xPos + INNER_PADDING, yPos + INNER_PADDING,
      width - (INNER_PADDING * 2), height - (INNER_PADDING * 2),
      1f, 5f, color
    )

    val imageY = yPos + 1f + (height - ICON_SIZE) / 2

    SkiaRenderer.image(
      icon, xPos + PADDING, imageY,
      ICON_SIZE, ICON_SIZE,
      color = color
    )

    val textX = xPos + ICON_SIZE + PADDING + 10f
    val textY = yPos + (height - NAME_SIZE) / 2

    SkiaRenderer.text(
      SkiaRenderer.regularFont, text,
      textX, textY,
      NAME_SIZE, color
    )
  }

  private val color = when (type) {
    Type.INFO -> theme.info
    Type.WARNING -> theme.warning
    Type.SUCCESS -> theme.success
    Type.ERROR -> theme.error
  }

  private val icon = SkiaRenderer.createImage(
    when (type) {
      Type.INFO -> "/assets/cobalt/ui/info.svg"
      Type.WARNING -> "/assets/cobalt/ui/warning.svg"
      Type.SUCCESS -> "/assets/cobalt/ui/success.svg"
      Type.ERROR -> "/assets/cobalt/ui/error.svg"
    }
  )

  enum class Type {
    INFO, WARNING, SUCCESS, ERROR
  }

  companion object {
    private const val INNER_PADDING = 5f
    private const val ICON_SIZE = 20f
  }

}


