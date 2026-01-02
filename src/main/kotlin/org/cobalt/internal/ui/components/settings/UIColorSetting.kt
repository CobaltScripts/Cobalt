package org.cobalt.internal.ui.components.settings

import java.awt.Color
import net.minecraft.client.input.CharInput
import org.cobalt.api.module.setting.impl.ColorSetting
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.ui.UIComponent
import org.cobalt.internal.ui.util.isHoveringOver
import org.cobalt.internal.ui.util.mouseX
import org.cobalt.internal.ui.util.mouseY

internal class UIColorSetting(private val setting: ColorSetting) : UIComponent(
  x = 0F,
  y = 0F,
  width = 627.5F,
  height = 60F,
) {

  private var showPicker = false
  private val pickerWidth = 260F
  private val paletteHeight = 180F
  private val sliderHeight = 20F

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

    val colorBoxX = x + width - 40F
    val colorBoxY = y + (height / 2F) - 15F
    val colorBoxSize = 30F

    NVGRenderer.rect(colorBoxX, colorBoxY, colorBoxSize, colorBoxSize, setting.value, 6F)
    NVGRenderer.hollowRect(colorBoxX, colorBoxY, colorBoxSize, colorBoxSize, 1.5F, Color(42, 42, 42).rgb, 6F)

    if (showPicker) {
      renderColorPicker(colorBoxX, colorBoxY)
    }
  }

  private fun renderColorPicker(refX: Float, refY: Float) {
    val pickerHeight = paletteHeight + sliderHeight + 36F
    val pickerX = refX - pickerWidth - 15F
    val pickerY = refY - pickerHeight - 10F

    NVGRenderer.rect(pickerX, pickerY, pickerWidth, pickerHeight, Color(25, 25, 25).rgb, 8F)
    NVGRenderer.hollowRect(pickerX, pickerY, pickerWidth, pickerHeight, 1.5F, Color(42, 42, 42).rgb, 8F)

    val paletteX = pickerX + 12F
    val paletteY = pickerY + 12F

    renderColorPalette(paletteX, paletteY, pickerWidth - 24F, paletteHeight)

    var currentY = paletteY + paletteHeight + 12F

    val alpha = (setting.value shr 24) and 0xFF
    val alphaPercent = (alpha / 255F * 100F).toInt()

    NVGRenderer.rect(paletteX, currentY, pickerWidth - 24F, sliderHeight, Color(40, 40, 40).rgb, 4F)
    NVGRenderer.hollowRect(paletteX, currentY, pickerWidth - 24F, sliderHeight, 1.5F, Color(42, 42, 42).rgb, 4F)

    val filledWidth = (pickerWidth - 24F) * (alpha.toFloat() / 255F)
    NVGRenderer.rect(paletteX, currentY, filledWidth, sliderHeight, Color(61, 94, 149).rgb, 4F)

    val sliderCenterX = paletteX + (pickerWidth - 24F) / 2F
    NVGRenderer.text(
      "α ${alphaPercent}%",
      sliderCenterX - 15F,
      currentY + 3F,
      12F,
      Color(200, 200, 200).rgb
    )
  }

  private fun renderColorPalette(x: Float, y: Float, width: Float, height: Float) {
    val hueSteps = 18
    val saturationSteps = 12

    val stepWidth = width / hueSteps
    val stepHeight = height / saturationSteps

    for (h in 0 until hueSteps) {
      for (s in 0 until saturationSteps) {
        val hue = h.toFloat() / hueSteps
        val saturation = s.toFloat() / saturationSteps
        val brightness = 1F - (s.toFloat() / saturationSteps) * 0.5F

        val colorInt = Color.HSBtoRGB(hue, saturation, brightness)

        val rectX = x + (h * stepWidth)
        val rectY = y + (s * stepHeight)

        NVGRenderer.rect(rectX, rectY, stepWidth, stepHeight, colorInt, 0F)
      }
    }
  }

  private fun getPickerBounds(colorBoxX: Float, colorBoxY: Float): PickerBounds {
    val pickerHeight = paletteHeight + sliderHeight + 36F
    val pickerX = colorBoxX - pickerWidth - 15F
    val pickerY = colorBoxY - pickerHeight - 10F
    val paletteX = pickerX + 12F
    val paletteY = pickerY + 12F
    return PickerBounds(pickerX, pickerY, pickerHeight, paletteX, paletteY)
  }

  override fun mouseClicked(button: Int): Boolean {
    if (button == 0) {
      val colorBoxX = x + width - 40F
      val colorBoxY = y + (height / 2F) - 15F

      if (isHoveringOver(colorBoxX, colorBoxY, 30F, 30F)) {
        showPicker = !showPicker
        return true
      }

      if (showPicker) {
        val bounds = getPickerBounds(colorBoxX, colorBoxY)

        if (isHoveringOver(bounds.paletteX, bounds.paletteY, pickerWidth - 24F, paletteHeight)) {
          return updateColorFromPalette(bounds.paletteX, bounds.paletteY, pickerWidth - 24F, paletteHeight)
        }

        val alphaSliderY = bounds.paletteY + paletteHeight + 28F
        if (isHoveringOver(bounds.paletteX, alphaSliderY, pickerWidth - 24F, sliderHeight)) {
          return updateAlphaFromSlider(bounds.paletteX, alphaSliderY, pickerWidth - 24F)
        }

        if (!isHoveringOver(bounds.pickerX, bounds.pickerY, pickerWidth, bounds.pickerHeight)) {
          showPicker = false
          return true
        }
        return true
      }
    }

    return false
  }

  override fun mouseDragged(button: Int, offsetX: Double, offsetY: Double): Boolean {
    if (button == 0 && showPicker) {
      val colorBoxX = x + width - 40F
      val colorBoxY = y + (height / 2F) - 15F
      val bounds = getPickerBounds(colorBoxX, colorBoxY)

      if (isHoveringOver(bounds.paletteX, bounds.paletteY, pickerWidth - 24F, paletteHeight)) {
        return updateColorFromPalette(bounds.paletteX, bounds.paletteY, pickerWidth - 24F, paletteHeight)
      }

      val alphaSliderY = bounds.paletteY + paletteHeight + 28F
      if (isHoveringOver(bounds.paletteX, alphaSliderY, pickerWidth - 24F, sliderHeight)) {
        return updateAlphaFromSlider(bounds.paletteX, alphaSliderY, pickerWidth - 24F)
      }
    }

    return false
  }

  private fun updateColorFromPalette(paletteX: Float, paletteY: Float, paletteWidth: Float, paletteHeight: Float): Boolean {
    val relX = (mouseX.toFloat() - paletteX).coerceIn(0F, paletteWidth)
    val relY = (mouseY.toFloat() - paletteY).coerceIn(0F, paletteHeight)

    val hue = relX / paletteWidth
    val saturation = relY / paletteHeight
    val brightness = 1F - (relY / paletteHeight) * 0.5F

    val colorInt = Color.HSBtoRGB(hue, saturation, brightness)
    val alpha = (setting.value shr 24) and 0xFF
    setting.value = (colorInt and 0xFFFFFF) or (alpha shl 24)
    return true
  }

  private fun updateAlphaFromSlider(sliderX: Float, sliderY: Float, sliderWidth: Float): Boolean {
    val relX = (mouseX.toFloat() - sliderX).coerceIn(0F, sliderWidth)
    val alpha = (relX / sliderWidth * 255F).toInt()

    val color = setting.value and 0xFFFFFF
    setting.value = color or (alpha shl 24)
    return true
  }

  override fun charTyped(input: CharInput): Boolean {
    if (!showPicker) return false

    if (input.codepoint().toChar() == '\u001B') {
      showPicker = false
      return true
    }

    return false
  }

  private data class PickerBounds(
    val pickerX: Float,
    val pickerY: Float,
    val pickerHeight: Float,
    val paletteX: Float,
    val paletteY: Float
  )
}
