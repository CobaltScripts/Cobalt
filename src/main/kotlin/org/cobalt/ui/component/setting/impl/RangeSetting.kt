package org.cobalt.ui.component.setting.impl

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.cobalt.ui.PADDING
import org.cobalt.ui.component.setting.Setting
import org.cobalt.util.input.Mouse

class RangeSetting(
  name: String,
  description: String,
  defaultValue: Pair<Int, Int>,
  val min: Int,
  val max: Int,
) : Setting<Pair<Int, Int>>(name, description, defaultValue) {

  override fun read(element: JsonElement) {
    if (!element.isJsonObject) return
    val obj = element.asJsonObject
    val start = obj.get("start")?.asInt ?: defaultValue.first
    val end = obj.get("end")?.asInt ?: defaultValue.second
    value = clamp(start, end)
    rawStart = value.first.toFloat()
    rawEnd = value.second.toFloat()
  }

  override fun write(): JsonElement = JsonObject().apply {
    add("start", JsonPrimitive(value.first))
    add("end", JsonPrimitive(value.second))
  }

  private var dragging = Knob.NONE
  private var rawStart: Float = defaultValue.first.toFloat()
  private var rawEnd: Float = defaultValue.second.toFloat()

  override val height: Float
    get() = BASE_HEIGHT + TrackSettingSupport.TRACK_ROW_HEIGHT

  override fun renderSetting() {
    val text = "${value.first} – ${value.second}"
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

    val geometry = trackGeometry()

    TrackSettingSupport.drawTrack(
      startX = geometry.startX,
      trackWidth = geometry.trackWidth,
      trackY = geometry.trackY,
      fillStartX = geometry.startKnobX,
      fillEndX = geometry.endKnobX,
      knobXs = listOf(geometry.startKnobX, geometry.endKnobX),
      trackBackground = theme.backgroundPrimary,
      fillColor = theme.accentPrimary,
      knobColor = theme.textPrimary
    )
  }

  override fun mouseClicked(button: Int): Boolean {
    if (button != 0) {
      return false
    }

    val geometry = trackGeometry()

    val knobRadius = TrackSettingSupport.KNOB_RADIUS

    dragging = when {
      Mouse.isHoveringOver(
        geometry.startKnobX - knobRadius,
        geometry.trackY - knobRadius,
        knobRadius * 2,
        knobRadius * 2
      ) -> Knob.START

      Mouse.isHoveringOver(
        geometry.endKnobX - knobRadius,
        geometry.trackY - knobRadius,
        knobRadius * 2,
        knobRadius * 2
      ) -> Knob.END

      else -> return false
    }

    updateValue()
    return true
  }

  override fun mouseDragged(button: Int, offsetX: Double, offsetY: Double): Boolean {
    if (dragging == Knob.NONE) {
      return false
    }

    updateValue()
    return true
  }

  override fun mouseReleased(button: Int): Boolean {
    if (dragging == Knob.NONE) {
      return false
    }

    dragging = Knob.NONE
    value = clamp(rawStart.toInt(), rawEnd.toInt())
    rawStart = value.first.toFloat()
    rawEnd = value.second.toFloat()
    return true
  }

  private fun updateValue() {
    val (startX, trackWidth) = trackGeometry()
    val rel = ((Mouse.mouseX - startX) / trackWidth).coerceIn(0f, 1f)
    val raw = (min + rel * (max - min)).coerceIn(min.toFloat(), max.toFloat())

    when (dragging) {
      Knob.START -> rawStart = raw.coerceAtMost(rawEnd - 1f)
      Knob.END -> rawEnd = raw.coerceAtLeast(rawStart + 1f)
      Knob.NONE -> return
    }

    var start = rawStart.toInt().coerceIn(min, max)
    var end = rawEnd.toInt().coerceIn(min, max)

    if (start >= end) {
      end = (start + 1).coerceAtMost(max)

      if (start >= end) {
        start = (end - 1).coerceAtLeast(min)
      }
    }

    value = start to end
  }

  private fun clamp(start: Int, end: Int): Pair<Int, Int> {
    var a = start.coerceIn(min, max)
    var b = end.coerceIn(min, max)

    if (a >= b) {
      if (a < max) {
        b = a + 1
      } else {
        a = b - 1
      }
    }

    return a to b
  }

  private fun trackGeometry(): TrackGeometry {
    val startX = xPos + PADDING
    val trackWidth = width - PADDING * 2
    val trackY = yPos + BASE_HEIGHT + TrackSettingSupport.TRACK_MARGIN
    val range = (max - min).toFloat().takeIf { it != 0f } ?: 1f
    val startKnobX = startX + (rawStart - min) / range * trackWidth
    val endKnobX = startX + (rawEnd - min) / range * trackWidth

    return TrackGeometry(
      startX = startX,
      trackWidth = trackWidth,
      trackY = trackY,
      startKnobX = startKnobX,
      endKnobX = endKnobX
    )
  }

  private data class TrackGeometry(
    val startX: Float,
    val trackWidth: Float,
    val trackY: Float,
    val startKnobX: Float,
    val endKnobX: Float,
  )

  private enum class Knob {
    START, END, NONE
  }

}
