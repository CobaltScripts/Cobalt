package org.cobalt.module.impl.script

import net.minecraft.client.Minecraft
import net.minecraft.network.protocol.Packet

open class ScriptState {

  protected val minecraft = Minecraft.getInstance()

  open fun enter() {}
  open fun onTick() {}
  open fun onRender() {}
  open fun onPacketSend(packet: Packet<*>) {}
  open fun onPacketReceive(packet: Packet<*>) {}
  open fun exit() {}

}
