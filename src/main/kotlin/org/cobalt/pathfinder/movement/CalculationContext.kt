package org.cobalt.pathfinder.movement

import net.minecraft.world.effect.MobEffects
import org.cobalt.Cobalt.minecraft
import org.cobalt.pathfinder.helper.BlockStateAccessor

class CalculationContext {

  val infCost = 1e6

  val level = minecraft.level!!
  val player = minecraft.player!!

  val speedAmplifier = player.getEffect(MobEffects.SPEED)?.amplifier ?: -1
  val jumpAmplifier = player.getEffect(MobEffects.JUMP_BOOST)?.amplifier ?: -1

  val blockStateAccessor = BlockStateAccessor(level)
}
