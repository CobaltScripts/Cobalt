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
      x = xPos + INNER_PADDING,
      y = yPos + INNER_PADDING,
      width = width - (INNER_PADDING * 2),
      height = height - (INNER_PADDING * 2),
      radius = 5f,
      color = color.updateAlpha(40)
    )

    SkiaRenderer.roundedOutline(
      x = xPos + INNER_PADDING,
      y = yPos + INNER_PADDING,
      width = width - (INNER_PADDING * 2),
      height = height - (INNER_PADDING * 2),
      thickness = 1f,
      radius = 5f,
      color = color
    )

    val imageY = yPos + 1f + (height - ICON_SIZE) / 2

    SkiaRenderer.image(
      image = icon,
      x = xPos + PADDING,
      y = imageY,
      width = ICON_SIZE,
      height = ICON_SIZE,
      color = color
    )

    val textX = xPos + ICON_SIZE + PADDING + 10f
    val textY = yPos + (height - NAME_SIZE) / 2

    SkiaRenderer.text(
      font = SkiaRenderer.regularFont,
      text = text,
      x = textX,
      y = textY,
      size = NAME_SIZE,
      color = color
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


