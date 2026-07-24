package org.cobalt.util.chat

import kotlin.math.roundToInt
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import org.cobalt.Cobalt
import org.cobalt.Cobalt.minecraft
import org.cobalt.Cobalt.runOnClientThread
import org.cobalt.module.impl.misc.DebugModule
import org.cobalt.util.color.blue
import org.cobalt.util.color.green
import org.cobalt.util.color.red
import org.slf4j.LoggerFactory

enum class MessageType {
  DEFAULT, RAW, DEBUG
}

object ChatUtils {

  private val logger = LoggerFactory.getLogger(this::class.java)

  private const val DARK_GRAY = "<dark_gray>"
  private const val RESET = "<reset>"
  private const val GRADIENT_END = "</gradient>"

  private const val DEFAULT_GRADIENT = "<gradient:#4CADD0:#B2F9FF>"
  private const val DEBUG_GRADIENT = "<gradient:#369876:#71FF9E>"

  private const val PREFIX_START = "$DARK_GRAY[$DARK_GRAY"
  private const val PREFIX_END = "$DARK_GRAY] $RESET"

  private val defaultPrefix =
    "$PREFIX_START$DEFAULT_GRADIENT${Cobalt.MOD_NAME}$GRADIENT_END$PREFIX_END"

  private val debugPrefix =
    "$PREFIX_START$DEBUG_GRADIENT${Cobalt.MOD_NAME} Debug$GRADIENT_END$PREFIX_END"

  private var lastDebugMessage: String? = null

  @JvmStatic
  fun sendSystemMessage(message: String, type: MessageType = MessageType.DEFAULT) {
    val component = when (type) {
      MessageType.DEFAULT -> ChatFormatter.parse(defaultPrefix + message)
      MessageType.RAW -> ChatFormatter.parse(message)
      MessageType.DEBUG -> {
        if (!DebugModule.enabled || lastDebugMessage == message) {
          return
        }

        lastDebugMessage = message
        ChatFormatter.parse(debugPrefix + message)
      }
    }

    addToChat(component)
  }

  @JvmStatic
  fun sendPlayerMessage(message: String) {
    runOnClientThread {
      val player = minecraft.player

      if (player == null) {
        logger.error("Attempted to send message as player ($message) but mc.player is null")
        return@runOnClientThread
      }

      player.connection.sendChat(message)
    }
  }

  @JvmStatic
  fun sendCommand(command: String) {
    runOnClientThread {
      val player = minecraft.player

      if (player == null) {
        logger.error("Attempted to send command ($command) but mc.player is null")
        return@runOnClientThread
      }

      player.connection.sendCommand(command)
    }
  }

  @JvmStatic
  fun buildGradientComponent(
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

  private fun addToChat(component: MutableComponent) {
    runOnClientThread {
      val player = minecraft.player

      if (player == null) {
        logger.error("Attempted to send system message but mc.player is null")
        return@runOnClientThread
      }

      player.sendSystemMessage(component)
    }
  }

}
