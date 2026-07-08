package org.cobalt.util

import kotlin.math.roundToInt
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import org.cobalt.dsl.alpha
import org.cobalt.dsl.blue
import org.cobalt.dsl.green
import org.cobalt.dsl.red

object ColorUtils {

  @JvmStatic
  fun buildTextGradient(
    text: String,
    startColor: Int,
    endColor: Int,
    baseStyle: Style = Style.EMPTY,
  ): MutableComponent {
    val result = Component.empty()
    val textLength = text.length

    if (textLength <= 1) {
      return Component
        .literal(text)
        .setStyle(baseStyle.withColor(TextColor.fromRgb(startColor)))
    }

    for (index in text.indices) {
      val denominator = (textLength - 1).toDouble()
      val ratio = index.toDouble() / denominator
      val red = (startColor.red + ratio * (endColor.red - startColor.red)).roundToInt()
      val green = (startColor.green + ratio * (endColor.green - startColor.green)).roundToInt()
      val blue = (startColor.blue + ratio * (endColor.blue - startColor.blue)).roundToInt()
      val interpolatedColor = (red shl 16) or (green shl 8) or blue

      val coloredChar = Component.literal(text[index].toString())
        .setStyle(baseStyle.withColor(TextColor.fromRgb(interpolatedColor)))

      result.append(coloredChar)
    }

    return result
  }

  @JvmStatic
  fun getRed(colorRgb: Int) = colorRgb.red

  @JvmStatic
  fun getGreen(colorRgb: Int) = colorRgb.green

  @JvmStatic
  fun getBlue(colorRgb: Int) = colorRgb.blue

  @JvmStatic
  fun getAlpha(colorRgb: Int) = colorRgb.alpha

}
