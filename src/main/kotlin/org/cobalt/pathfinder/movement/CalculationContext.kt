package org.cobalt.pathfinder.movement

import net.minecraft.world.effect.MobEffects
import org.cobalt.Cobalt.minecraft
import org.cobalt.pathfinder.cost.ActionCosts
import org.cobalt.pathfinder.helper.BlockStateAccessor

class CalculationContext {

  val level = minecraft.level!!
  val player = minecraft.player!!

  val maxFallDistance = 20

  val speedAmplifier = player.getEffect(MobEffects.SPEED)?.amplifier ?: -1
  val jumpAmplifier = player.getEffect(MobEffects.JUMP_BOOST)?.amplifier ?: -1

  val costs = ActionCosts(this, speedAmplifier, jumpAmplifier)
  val bsa = BlockStateAccessor(level)

}
