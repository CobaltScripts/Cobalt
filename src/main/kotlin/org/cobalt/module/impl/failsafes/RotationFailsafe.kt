package org.cobalt.module.impl.failsafes

import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.PacketEvent
import org.cobalt.module.type.Failsafe
import org.cobalt.util.client.PlayerUtils.player
import org.cobalt.util.failsafe.FailsafeManager
import org.cobalt.util.rotation.data.Rotation

object RotationFailsafe : Failsafe("Rotation", 10, false) {
  @SubscribeEvent
  fun onRotation(event: PacketEvent.Any) {
    if (!shouldReactToEvents()) return
    if (minecraft.level == null) return

    when (val packet = event.packet) {
      is ClientboundPlayerPositionPacket -> handlePositionPacket(packet)
      is ServerboundChatCommandPacket -> handleChatCommandPacket(packet)
    }
  }

  private fun handlePositionPacket(packet: ClientboundPlayerPositionPacket) {
    val currentRotation = try {
      Rotation(player!!.yRot, player!!.xRot)
    } catch (_: NullPointerException) {
      return
    }

    val newRotation = Rotation(packet.change.yRot, packet.change.xRot)

    if (currentRotation == newRotation) {
      return
    }

    FailsafeManager.alertUser(
      this,
      "<red>FROM</red> <yellow>Pitch: ${currentRotation.pitch}, Yaw: ${currentRotation.yaw}</yellow>" +
        " <red>TO</red>" +
        " <yellow>Pitch: ${newRotation.pitch}, Yaw: ${newRotation.yaw}</yellow>"
    )
  }

  private fun handleChatCommandPacket(packet: ServerboundChatCommandPacket) {
    if (!packet.command.contains("warp")) return
    FailsafeManager.ignoreFailsafe(this)
  }

  override fun resetStates() {
    return
  }

  override fun performReaction(): ReactionResult? {
    // TODO
    return null
  }
}
