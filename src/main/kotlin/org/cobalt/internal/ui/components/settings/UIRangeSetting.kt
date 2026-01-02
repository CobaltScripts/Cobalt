package org.cobalt.internal.ui.components.settings

import java.awt.Color
import kotlin.math.abs
import org.cobalt.api.module.setting.impl.RangeSetting
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.ui.UIComponent
import org.cobalt.internal.ui.util.isHoveringOver
import org.cobalt.internal.ui.util.mouseX

internal class UIRangeSetting(private val setting: RangeSetting) : UIComponent(
  x = 0F,
  y = 0F,
  width = 627.5F,
  height = 60F,
) {

  private var isDraggingStart = false
  private var isDraggingEnd = false

  private fun getValueFromX(mouseX: Double): Double {
    val relativeX = (mouseX - (x + width - 220F)).coerceIn(0.0, 200.0)
    val percentage = relativeX / 200F
    return setting.min + (percentage * (setting.max - setting.min))
  }

  private fun getThumbX(value: Double): Float {
    val percentage = (value - setting.min) / (setting.max - setting.min)
    return (x + width - 220F) + (percentage * 200F).toFloat()
  }

  override fun render() {
    NVGRenderer.rect(x, y, width, height, Color(42, 42, 42, 50).rgb, 10F)
    NVGRenderer.hollowRect(x, y, width, height, 1F, Color(42, 42, 42).rgb, 10F)

    NVGRenderer.text(
      setting.name,
      x + 20F,
      y + (height / 2F) - 15.5F,
      15F,
      Color(230, 230, 230).rgb
    )

    NVGRenderer.text(
      setting.description,
      x + 20F,
      y + (height / 2F) + 2F,
      12F,
      Color(179, 179, 179).rgb
    )

    val sliderX = x + width - 220F
    val sliderY = y + (height / 2F) - 2F
    val startThumbX = getThumbX(setting.value.first)
    val endThumbX = getThumbX(setting.value.second)

    NVGRenderer.rect(sliderX, sliderY, 200F, 4F, Color(60, 60, 60).rgb, 2F)

    NVGRenderer.rect(
      startThumbX,
      sliderY,
      endThumbX - startThumbX,
      4F,
      Color(61, 94, 149).rgb,
      2F
    )

    NVGRenderer.circle(startThumbX, sliderY + 2F, 6F, Color(61, 94, 149).rgb)
    NVGRenderer.circle(endThumbX, sliderY + 2F, 6F, Color(61, 94, 149).rgb)

    val text = String.format("%.2f - %.2f", setting.value.first, setting.value.second)
    val textWidth = NVGRenderer.textWidth(text, 13F)

    NVGRenderer.rect(
      sliderX - textWidth - 26F,
      y + (height / 2F) - 12F,
      textWidth + 16F,
      24F,
      Color(42, 42, 42, 50).rgb,
      4F
    )
    NVGRenderer.hollowRect(
      sliderX - textWidth - 26F,
      y + (height / 2F) - 12F,
      textWidth + 16F,
      24F,
      1F,
      Color(42, 42, 42).rgb,
      4F
    )

    NVGRenderer.text(
      text,
      sliderX - textWidth - 18F,
      y + (height / 2F) - 6F,
      13F,
      Color(200, 200, 200).rgb
    )
  }

  override fun mouseClicked(button: Int): Boolean {
    if (button == 0) {
      val sliderX = x + width - 220F
      val sliderY = y + (height / 2F) - 2F
      val startThumbX = getThumbX(setting.value.first)
      val endThumbX = getThumbX(setting.value.second)

      if (isHoveringOver(startThumbX - 6F, sliderY - 4F, 12F, 12F)) {
        isDraggingStart = true
        return true
      }

      if (isHoveringOver(endThumbX - 6F, sliderY - 4F, 12F, 12F)) {
        isDraggingEnd = true
        return true
      }

      if (isHoveringOver(sliderX, sliderY - 5F, 200F, 14F)) {
        val clickedValue = getValueFromX(mouseX)
        val distToStart = abs(clickedValue - setting.value.first)
        val distToEnd = abs(clickedValue - setting.value.second)

        if (distToStart < distToEnd) {
          setting.value = Pair(clickedValue, setting.value.second)
          isDraggingStart = true
        } else {
          setting.value = Pair(setting.value.first, clickedValue)
          isDraggingEnd = true
        }
        return true
      }
    }

    return false
  }

  override fun mouseDragged(button: Int, offsetX: Double, offsetY: Double): Boolean {
    if (button == 0) {
      val newValue = getValueFromX(mouseX)

      if (isDraggingStart) {
        setting.value = Pair(
          newValue.coerceAtMost(setting.value.second),
          setting.value.second
        )
        return true
      }

      if (isDraggingEnd) {
        setting.value = Pair(
          setting.value.first,
          newValue.coerceAtLeast(setting.value.first)
        )
        return true
      }
    }

    return false
  }

  override fun mouseReleased(button: Int): Boolean {
    if (button == 0 && (isDraggingStart || isDraggingEnd)) {
      isDraggingStart = false
      isDraggingEnd = false
      return true
    }

    return false
  }

}
