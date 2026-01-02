package org.cobalt.api.event.impl.client

import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import org.cobalt.api.event.Event

@Suppress("UNUSED_PARAMETER")
abstract class ChatEvent(val packet: Packet<*>) : Event(true) {

  class Receive(packet: Packet<*>) : ChatEvent(packet) {
    val message: String? = when (packet) {
      is GameMessageS2CPacket -> packet.content().string
      else -> null
    }
  }

  class Send(packet: Packet<*>) : ChatEvent(packet) {
    val message: String? = when (packet) {
      is ChatMessageC2SPacket -> packet.chatMessage
      else -> null
    }
  }

}
