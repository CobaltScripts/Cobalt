package org.cobalt.pathfinder.movement

import kotlin.math.max
import net.minecraft.world.effect.MobEffects
import org.cobalt.Cobalt.minecraft
import org.cobalt.pathfinder.cost.ActionCosts
import org.cobalt.pathfinder.helper.BlockStateAccessor

class CalculationContext {

  val level = minecraft.level!!
  val player = minecraft.player!!

  val jumpAmplifier = player.getEffect(MobEffects.JUMP_BOOST)?.amplifier ?: 0

  val costs = ActionCosts(this)
  val bsa = BlockStateAccessor(level)

  val maxFallDistance = 20
  val maxJumpBlock: Int = max(2, 2 + (jumpAmplifier * 2 + 1) / 4)

}
