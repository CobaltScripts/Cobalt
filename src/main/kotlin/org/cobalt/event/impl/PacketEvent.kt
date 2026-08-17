package org.cobalt.event.impl

import net.minecraft.network.protocol.Packet
import org.cobalt.event.Event

abstract class PacketEvent(
  val packet: Packet<*>,
) : Event.Cancellable() {

  open class Any(packet: Packet<*>) : PacketEvent(packet)

  class Send(packet: Packet<*>) : Any(packet)
  class Receive(packet: Packet<*>) : Any(packet)
}
