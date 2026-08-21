package org.cobalt.module.impl.failsafes

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import org.cobalt.Cobalt
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.TickEvent
import org.cobalt.module.ModuleManager
import org.cobalt.module.type.Failsafe
import org.cobalt.util.client.PlayerUtils
import org.cobalt.util.failsafe.FailsafeManager

object PlayerCheckFailsafe: Failsafe("Player Check", 10, false) {
  private val offenders = mutableMapOf<String, Int>()

  @SubscribeEvent
  fun onTick(ignored: TickEvent.End) {
    if (!ModuleManager.isScriptRunning() && !FabricLoader.getInstance().isDevelopmentEnvironment) return

    val player = PlayerUtils.player ?: return
    val level = Cobalt.minecraft.level ?: return
    val playerPosition = BlockPos(player.x.toInt(), player.y.toInt(), player.z.toInt())

    for (entity in level.entitiesForRendering()) {
      if (entity.uuid.version() == 2) continue // npc version?? i think?? this might be outdated
      if (entity == player) continue

      val entityPosition = BlockPos(entity.x.toInt(), entity.y.toInt(), entity.z.toInt())

      val entityIgn = entity.name.string

      if (playerPosition == entityPosition) {
        val ticks = (offenders[entityIgn] ?: 0) + 1
        offenders[entityIgn] = ticks

        when (ticks) {
          10 -> {
            FailsafeManager.alertUser(
              this,
              "<yellow>$entityIgn</yellow> <red>IS STANDING INSIDE YOU</red>"
            )
          }

          50 -> {
            FailsafeManager.alertUser(
              this,
              "<red>$entityIgn IS STILL STANDING INSIDE YOU</red> " +
                "<yellow><b>REACT NOW</b></yellow>"
            )
          }

          100 -> {
            FailsafeManager.alertUser(
              this,
              "<red><b><u>$entityIgn IS STILL STANDING INSIDE YOU</b></u></red> " +
                "<grey>(resetting tick counter)</grey>"
            )

            offenders[entityIgn] = 0
          }
        }
      }
    }
  }

  override fun resetStates() {
    offenders.clear()
  }

  override fun performReaction(): ReactionResult? {
    TODO("Not yet implemented")
  }

}
