package org.cobalt.api.event.impl.client

import net.minecraft.network.packet.Packet
import org.cobalt.api.event.Event

@Suppress("UNUSED_PARAMETER")
abstract class PacketEvent(val packet: Packet<*>): Event(true) {

  class Incoming(packet: Packet<*>): PacketEvent(packet)
  class Outgoing(packet: Packet<*>): PacketEvent(packet)

}
