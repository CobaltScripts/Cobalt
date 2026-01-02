package org.cobalt.api.util

import kotlin.math.roundToInt
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.Formatting
import org.cobalt.Cobalt.mc

object ChatUtils {

  /**
   * Function to display a message in Minecraft chat with the prefix "[Cobalt Debug]"
   *
   * @param message The string to send to chat after the prefix
   */
  @JvmStatic
  fun sendDebug(message: String) {
    mc.inGameHud.chatHud.addMessage(
      Text.empty().append(debugPrefix)
        .append(Text.literal("${Formatting.RESET}$message"))
    )
  }

  /**
   * Function to display a message in Minecraft chat with the prefix "[Cobalt]" (default prefix)
   *
   * @param message The string to send to chat after the prefix
   */
  @JvmStatic
  fun sendMessage(message: String) {
    mc.inGameHud.chatHud.addMessage(
      Text.empty().append(prefix)
        .append(Text.literal("${Formatting.RESET}$message"))
    )
  }

  /**
   * Function to create a gradient
   *
   * @param text The string for the gradient
   * @param startRgb The color for the start of the gradient, Int
   * @param endRgb The color for the end of the gradient, Int
   *
   * @return Text object with a gradient with the specified colors
   */
  @JvmStatic
  fun buildGradient(text: String, startRgb: Int, endRgb: Int): MutableText {
    val result = Text.empty()
    val length = text.length

    if (length <= 1) {
      return Text.literal(text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(startRgb)))
    }

    val sr = (startRgb shr 16) and 0xFF
    val sg = (startRgb shr 8) and 0xFF
    val sb = startRgb and 0xFF

    val er = (endRgb shr 16) and 0xFF
    val eg = (endRgb shr 8) and 0xFF
    val eb = endRgb and 0xFF

    for (i in text.indices) {
      val ratio = i.toDouble() / (length - 1)

      val r = (sr + ratio * (er - sr)).roundToInt()
      val g = (sg + ratio * (eg - sg)).roundToInt()
      val b = (sb + ratio * (eb - sb)).roundToInt()

      val rgb = (r shl 16) or (g shl 8) or b

      val charText = Text.literal(text[i].toString())
        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)))

      result.append(charText)
    }

    return result
  }

  private val prefix = Text.literal("${Formatting.DARK_GRAY}[")
    .append(buildGradient("Cobalt", 0x4CADD0, 0xB2F9FF))
    .append(Text.literal("${Formatting.DARK_GRAY}] "))

  private val debugPrefix = Text.literal("${Formatting.DARK_GRAY}[")
    .append(buildGradient("Cobalt Debug", 0x369876, 0x71FF9E))
    .append(Text.literal("${Formatting.DARK_GRAY}] "))

}
