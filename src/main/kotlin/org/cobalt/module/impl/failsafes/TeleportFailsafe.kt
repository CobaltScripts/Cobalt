package org.cobalt.module.impl.failsafes

import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
import org.cobalt.Cobalt.minecraft
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.PacketEvent
import org.cobalt.module.type.Failsafe
import org.cobalt.util.failsafe.FailsafeManager

object TeleportFailsafe: Failsafe("Teleport", 10, true) {
  @SubscribeEvent
  fun onTeleport(event: PacketEvent.Receive) {
    if (event.packet !is ClientboundPlayerPositionPacket) return

    FailsafeManager.alertUser(this)
  }

  override fun resetStates() {
    TODO("Not yet implemented")
  }

  override fun performReaction(): ReactionResult {
    TODO("Not yet implemented")
  }

}
