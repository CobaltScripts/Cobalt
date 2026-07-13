package org.cobalt.util

import net.minecraft.network.chat.MutableComponent
import org.cobalt.Cobalt
import org.cobalt.Cobalt.minecraft
import org.cobalt.Cobalt.runOnClientThread
import org.cobalt.module.impl.misc.Debug
import org.cobalt.util.helper.ChatFormatter
import org.slf4j.LoggerFactory

object ChatUtils {

  private val logger = LoggerFactory.getLogger(this::class.java)

  private const val DARK_GRAY = "<dark_gray>"
  private const val RESET = "<reset>"
  private const val GRADIENT_END = "</gradient>"

  private const val DEFAULT_GRADIENT = "<gradient:#4CADD0:#B2F9FF>"
  private const val DEBUG_GRADIENT = "<gradient:#369876:#71FF9E>"

  private const val PREFIX_START = "$DARK_GRAY[$DARK_GRAY]"
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
        if (!Debug.enabled || lastDebugMessage == message) {
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

enum class MessageType {
  DEFAULT,
  RAW,
  DEBUG
}
