package org.cobalt.module.impl.failsafes

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.Vec3
import org.cobalt.Cobalt
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.PacketEvent
import org.cobalt.module.ModuleManager
import org.cobalt.module.type.Failsafe
import org.cobalt.util.client.PlayerUtils
import org.cobalt.util.failsafe.FailsafeManager

object VelocityFailsafe: Failsafe("Velocity", 10, false) {

  private val bouncePadVelocities = setOf(
    1.4999694805591162,
    2.1499725325032046,
    2.0,
    2.6000732466581207,
    1.3500579869376792,
    1.7000549349935907,
    2.09998168833547,
    1.5999511688945858
  )

  @SubscribeEvent
  fun onVelo(event: PacketEvent.Receive) {
    if (!ModuleManager.isScriptRunning() && !FabricLoader.getInstance().isDevelopmentEnvironment) return
    val player = Cobalt.minecraft.player ?: return
    val packet = event.packet as? ClientboundSetEntityMotionPacket ?: return

    if (packet.id != player.id) return

    val velocity = packet.movement
    if (velocity == Vec3.ZERO) return
    val state = minecraft.level?.getBlockState(PlayerUtils.blockStandingOn) ?: return

    if (state.block == Blocks.SLIME_BLOCK && velocity.y in bouncePadVelocities) return

    FailsafeManager.alertUser(
      this,
      "<red>VELOCITY CHANGE</red>: " +
        "<yellow>${"%.2f".format(velocity.x)}, ${"%.2f".format(velocity.y)}, ${"%.2f".format(velocity.z)}</yellow>"
    )
  }


  override fun resetStates() {
    TODO("Not yet implemented")
  }

  override fun performReaction(): ReactionResult {
    TODO("Not yet implemented")
  }

}
