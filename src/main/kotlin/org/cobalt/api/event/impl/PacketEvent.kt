package org.cobalt.api.event.impl

import org.cobalt.api.event.Event
import net.minecraft.network.packet.Packet

abstract class PacketEvent(val packet: Packet<*>): Event(true) {
  class Incoming(packet: Packet<*>): PacketEvent(packet)
  class Outgoing(packet: Packet<*>): PacketEvent(packet)
}
