package org.cobalt.module.impl.failsafes

import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundPlayerRotationPacket
import org.cobalt.Cobalt
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.PacketEvent
import org.cobalt.module.ModuleManager
import org.cobalt.module.type.Failsafe
import org.cobalt.util.failsafe.FailsafeManager
import org.cobalt.util.rotation.data.Rotation

object RotationFailsafe: Failsafe("Rotation", 10, true) {
  fun onRotation(currentRot: Rotation, newRot: Rotation) {
      val player = Cobalt.minecraft.player ?: return

      if (currentRot == newRot) return // I think this is needed? not sure

      FailsafeManager.alertUser(
        this,
        "ROTATED FROM ${currentRot.pitch} & ${currentRot.yaw} TO ${newRot.pitch} & ${newRot.yaw}"
      )
    }

  override fun resetStates() {
    TODO("Not yet implemented")
  }

  override fun performReaction(): ReactionResult? {
    TODO("Not yet implemented")
  }
}
