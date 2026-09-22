package org.cobalt.ui.component.setting.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import org.cobalt.ui.PADDING
import org.cobalt.ui.component.setting.Setting
import org.cobalt.util.input.Mouse

class SliderSetting(
  name: String,
  description: String,
  defaultValue: Int,
  val min: Int,
  val max: Int,
) : Setting<Int>(name, description, defaultValue) {

  override val height: Float
    get() = BASE_HEIGHT + TrackSettingSupport.TRACK_ROW_HEIGHT

  override fun read(element: JsonElement) {
    value = element.asInt.coerceIn(min, max)
    rawValue = value.toFloat()
  }

  override fun write(): JsonElement = JsonPrimitive(value)

  private var dragging = false
  private var rawValue: Float = defaultValue.toFloat()

  override fun renderSetting() {
    val text = value.toString()
    val boxWidth = TrackSettingSupport.valueBoxWidth(text)
    val boxX = xPos + width - PADDING - boxWidth
    val boxY = yPos + (BASE_HEIGHT - TrackSettingSupport.VALUE_BOX_HEIGHT) / 2

    TrackSettingSupport.drawValueBox(
      x = boxX,
      y = boxY,
      width = boxWidth,
      text = text,
      background = theme.backgroundPrimary,
      border = theme.border,
      textColor = theme.textPrimary
    )

    val (startX, trackWidth, trackY, knobX) = trackGeometry()

    TrackSettingSupport.drawTrack(
      startX = startX,
      trackWidth = trackWidth,
      trackY = trackY,
      fillStartX = startX,
      fillEndX = knobX,
      knobXs = listOf(knobX),
      trackBackground = theme.backgroundPrimary,
      fillColor = theme.accentPrimary,
      knobColor = theme.textPrimary
    )
  }

  override fun mouseClicked(button: Int): Boolean {
    if (button != 0) {
      return false
    }

    val (_, _, trackY, knobX) = trackGeometry()
    val knobRadius = TrackSettingSupport.KNOB_RADIUS

    if (
      !Mouse.isHoveringOver(
        knobX - knobRadius, trackY - knobRadius,
        knobRadius * 2, knobRadius * 2
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
    val trackY = yPos + BASE_HEIGHT + TrackSettingSupport.TRACK_MARGIN
    val range = (max - min).toFloat().takeIf { it != 0f } ?: 1f
    val displayValue = if (dragging) rawValue else value.toFloat()
    val knobX = startX + (displayValue - min) / range * trackWidth
    return TrackGeometry(startX, trackWidth, trackY, knobX)
  }

  private data class TrackGeometry(
    val startX: Float,
    val trackWidth: Float,
    val trackY: Float,
    val knobX: Float,
  )

}
