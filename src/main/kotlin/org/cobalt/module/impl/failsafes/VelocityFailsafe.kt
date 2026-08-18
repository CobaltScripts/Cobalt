package org.cobalt.module.impl.failsafes

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket
import net.minecraft.world.phys.Vec3
import org.cobalt.Cobalt
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.PacketEvent
import org.cobalt.module.type.Failsafe
import org.cobalt.util.client.PlayerUtils
import org.cobalt.util.failsafe.FailsafeManager

object VelocityFailsafe: Failsafe("Velocity", 10, true) {
  @SubscribeEvent
  fun onVelo(event: PacketEvent.Receive) {
    if (Cobalt.minecraft.player == null) return
    if (event.packet !is ClientboundSetEntityMotionPacket) return
    if (event.packet.id != Cobalt.minecraft.player!!.id) return
    val packet = event.packet
    val packetvelo = event.packet.movement

    val bouncePadVelocities = listOf<Double>(
      1.4999694805591162,
      2.1499725325032046,
      2.0,
      2.6000732466581207,
      1.3500579869376792,
      1.7000549349935907,
      2.09998168833547,
    )

    if (packetvelo == Vec3(0.0,0.0,0.0)) return

    val state = minecraft.level?.getBlockState(PlayerUtils.blockStandingOn) ?: return
    val blockUnderName = state.block.name.string

    if (blockUnderName == "Slime Block" && packetvelo.y in bouncePadVelocities) return

    FailsafeManager.alertUser(this, "VELOCITY CHANGE: $packetvelo")
  }


  override fun resetStates() {
    TODO("Not yet implemented")
  }

  override fun performReaction(): ReactionResult {
    TODO("Not yet implemented")
  }

}
