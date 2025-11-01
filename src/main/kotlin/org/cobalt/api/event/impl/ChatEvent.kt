package org.cobalt.api.event.impl

import org.cobalt.api.event.Event
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket

abstract class ChatEvent(val packet: Packet<*>) : Event(true) {

  class Receive(packet: Packet<*>) : ChatEvent(packet) {
    val message: String = when (packet) {
      is GameMessageS2CPacket -> packet.content().string
      else -> "no message"
    }
  }

  class Send(packet: Packet<*>) : ChatEvent(packet) {
    val message: String = when (packet) {
      is ChatMessageC2SPacket -> packet.chatMessage
      else -> "no message"
    }
  }
}
