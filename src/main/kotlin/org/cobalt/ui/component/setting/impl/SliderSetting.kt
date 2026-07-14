package org.cobalt.ui.component.setting.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import org.cobalt.ui.component.setting.Setting
import org.cobalt.util.input.Mouse
import org.cobalt.util.render.SkiaRenderer

class SliderSetting(
  name: String,
  description: String,
  defaultValue: Int,
  val min: Int,
  val max: Int,
) : Setting<Int>(name, description, defaultValue) {

  override val height: Float
    get() = BASE_HEIGHT + TRACK_ROW_HEIGHT

  override fun read(element: JsonElement) {
    value = element.asInt.coerceIn(min, max)
    rawValue = value.toFloat()
  }

  override fun write(): JsonElement = JsonPrimitive(value)

  private var dragging = false
  private var rawValue: Float = defaultValue.toFloat()

  override fun renderSetting() {
    val text = value.toString()
    val boxWidth = valueBoxWidth(text)
    val boxX = xPos + width - PADDING - boxWidth
    val boxY = yPos + (BASE_HEIGHT - VALUE_BOX_HEIGHT) / 2

    SkiaRenderer.roundedRect(
      boxX, boxY, boxWidth, VALUE_BOX_HEIGHT,
      5f, theme.backgroundPrimary
    )

    SkiaRenderer.roundedOutline(
      boxX, boxY, boxWidth, VALUE_BOX_HEIGHT,
      1f, 5f, theme.border
    )

    val textWidth = SkiaRenderer.textWidth(SkiaRenderer.regularFont, text, FONT_SIZE)

    SkiaRenderer.text(
      SkiaRenderer.regularFont, text,
      boxX + (boxWidth - textWidth) / 2,
      boxY + (VALUE_BOX_HEIGHT - FONT_SIZE) / 2,
      FONT_SIZE, theme.textPrimary
    )

    val (startX, trackWidth, trackY, knobX) = trackGeometry()

    SkiaRenderer.roundedRect(
      startX, trackY - 2f,
      trackWidth, 4f,
      3f, theme.backgroundPrimary
    )

    SkiaRenderer.roundedRect(
      startX, trackY - 2f,
      (knobX - startX).coerceAtLeast(0f), 4f,
      3f, theme.accentPrimary
    )

    SkiaRenderer.circle(
      knobX, trackY,
      KNOB_RADIUS, theme.textPrimary
    )
  }

  override fun mouseClicked(button: Int): Boolean {
    if (button != 0) {
      return false
    }

    val (_, _, trackY, knobX) = trackGeometry()

    if (
      !Mouse.isHoveringOver(
        knobX - KNOB_RADIUS, trackY - KNOB_RADIUS,
        KNOB_RADIUS * 2, KNOB_RADIUS * 2
      )
    ) {
      return false
    }

    dragging = true
    updateValue()
    return true
  }

  override fun mouseDragged(button: Int, offsetX: Double, offsetY: Double): Boolean {
    if (!dragging) {
      return false
    }

    updateValue()
    return true
  }

  override fun mouseReleased(button: Int): Boolean {
    if (!dragging) {
      return false
    }

    dragging = false
    value = rawValue.toInt().coerceIn(min, max)
    rawValue = value.toFloat()
    return true
  }

  private fun updateValue() {
    val (startX, trackWidth) = trackGeometry()
    val rel = ((Mouse.mouseX - startX) / trackWidth).coerceIn(0f, 1f)

    rawValue = (min + rel * (max - min)).coerceIn(min.toFloat(), max.toFloat())
    value = rawValue.toInt().coerceIn(min, max)
  }

  private fun trackGeometry(): TrackGeometry {
    val startX = xPos + PADDING
    val trackWidth = width - PADDING * 2
    val trackY = yPos + BASE_HEIGHT + TRACK_MARGIN
    val range = (max - min).toFloat().takeIf { it != 0f } ?: 1f
    val displayValue = if (dragging) rawValue else value.toFloat()
    val knobX = startX + (displayValue - min) / range * trackWidth
    return TrackGeometry(startX, trackWidth, trackY, knobX)
  }

  private fun valueBoxWidth(text: String): Float =
    SkiaRenderer.textWidth(SkiaRenderer.regularFont, text, FONT_SIZE) + VALUE_BOX_PADDING_X * 2f

  private data class TrackGeometry(
    val startX: Float,
    val trackWidth: Float,
    val trackY: Float,
    val knobX: Float,
  )

  companion object {
    private const val KNOB_RADIUS = 5f
    private const val FONT_SIZE = 12f
    private const val VALUE_BOX_HEIGHT = 30f
    private const val VALUE_BOX_PADDING_X = 14f
    private const val TRACK_ROW_HEIGHT = 15f
    private const val TRACK_MARGIN = 5f
  }

}
