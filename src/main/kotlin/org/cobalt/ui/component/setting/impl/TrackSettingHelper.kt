package org.cobalt.ui.component.setting.impl

import java.awt.Color
import org.cobalt.util.render.SkiaRenderer

/** Value-box and track rendering shared by RangeSetting (two knobs) and SliderSetting (one knob). */
internal object TrackSettingHelper {

  const val KNOB_RADIUS = 5f
  const val FONT_SIZE = 12f
  const val VALUE_BOX_HEIGHT = 30f
  const val VALUE_BOX_PADDING_X = 14f
  const val TRACK_ROW_HEIGHT = 15f
  const val TRACK_MARGIN = 5f

  fun valueBoxWidth(text: String): Float =
    SkiaRenderer.textWidth(SkiaRenderer.regularFont, text, FONT_SIZE) + VALUE_BOX_PADDING_X * 2f

  fun drawValueBox(x: Float, y: Float, width: Float, text: String, background: Color, border: Color, textColor: Color) {
    SkiaRenderer.roundedRect(
      x = x,
      y = y,
      width = width,
      height = VALUE_BOX_HEIGHT,
      radius = 5f,
      color = background
    )

    SkiaRenderer.roundedOutline(
      x = x,
      y = y,
      width = width,
      height = VALUE_BOX_HEIGHT,
      thickness = 1f,
      radius = 5f,
      color = border
    )

    val textWidth = SkiaRenderer.textWidth(SkiaRenderer.regularFont, text, FONT_SIZE)

    SkiaRenderer.text(
      font = SkiaRenderer.regularFont,
      text = text,
      x = x + (width - textWidth) / 2,
      y = y + (VALUE_BOX_HEIGHT - FONT_SIZE) / 2,
      size = FONT_SIZE,
      color = textColor
    )
  }

  fun drawTrack(
    startX: Float,
    trackWidth: Float,
    trackY: Float,
    fillStartX: Float,
    fillEndX: Float,
    knobXs: List<Float>,
    trackBackground: Color,
    fillColor: Color,
    knobColor: Color,
  ) {
    SkiaRenderer.roundedRect(
      x = startX,
      y = trackY - 2f,
      width = trackWidth,
      height = 4f,
      radius = 3f,
      color = trackBackground
    )

    SkiaRenderer.roundedRect(
      x = fillStartX,
      y = trackY - 2f,
      width = (fillEndX - fillStartX).coerceAtLeast(0f),
      height = 4f,
      radius = 3f,
      color = fillColor
    )

    knobXs.forEach { knobX ->
      SkiaRenderer.circle(knobX, trackY, KNOB_RADIUS, knobColor)
    }
  }

}
