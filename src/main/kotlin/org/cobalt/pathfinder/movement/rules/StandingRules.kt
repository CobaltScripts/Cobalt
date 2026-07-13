package org.cobalt.pathfinder.movement.rules

import net.minecraft.world.level.block.AzaleaBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.CarpetBlock
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.StainedGlassBlock
import net.minecraft.world.level.block.StairBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Fluids
import org.cobalt.pathfinder.helper.BlockStateAccessor
import org.cobalt.pathfinder.movement.rules.data.BlockSetRule
import org.cobalt.pathfinder.movement.rules.data.BlockTypeRule
import org.cobalt.pathfinder.movement.rules.data.Ternary
import org.cobalt.util.BlockUtils

object StandingRules {

  val STANDING_RULES = listOf(
    BlockSetRule(
      setOf(
        Blocks.LADDER,
        Blocks.VINE,
        Blocks.FARMLAND,
        Blocks.DIRT_PATH,
        Blocks.SOUL_SAND,
        Blocks.ENDER_CHEST,
        Blocks.CHEST,
        Blocks.TRAPPED_CHEST,
        Blocks.GLASS,
      ),
      Ternary.YES,
    ),

    BlockTypeRule(
      setOf(
        StainedGlassBlock::class.java,
        StairBlock::class.java,
        SlabBlock::class.java,
        AzaleaBlock::class.java,
      ),
      Ternary.YES,
    ),
  )

  private val PRECOMPUTED = Array(Block.BLOCK_STATE_REGISTRY.size()) {
    Ternary.NO
  }

  init {
    Block.BLOCK_STATE_REGISTRY.forEach { state ->
      PRECOMPUTED[Block.BLOCK_STATE_REGISTRY.getId(state)] = compute(state)
    }
  }

  private fun compute(state: BlockState): Ternary {
    val block = state.block

    if (BlockUtils.isBlockNormalCube(state) &&
      block != Blocks.BUBBLE_COLUMN &&
      block != Blocks.HONEY_BLOCK
    ) {
      return Ternary.YES
    }

    STANDING_RULES.firstOrNull { it.matches(state) }
      ?.let { return it.result }

    return when {
      BlockUtils.isWater(state) -> Ternary.MAYBE
      BlockUtils.isLava(state) -> Ternary.MAYBE
      else -> Ternary.NO
    }
  }

  fun canStandOn(bsa: BlockStateAccessor, x: Int, y: Int, z: Int): Boolean {
    val state = bsa.get(x, y, z)
    val result = PRECOMPUTED[Block.BLOCK_STATE_REGISTRY.getId(state)]

    if (result == Ternary.YES) {
      return true
    }

    if (result == Ternary.NO) {
      return false
    }

    return resolveMaybe(bsa, x, y, z, state)
  }

  private fun resolveMaybe(bsa: BlockStateAccessor, x: Int, y: Int, z: Int, state: BlockState): Boolean {
    if (BlockUtils.isWater(state)) {
      val upState = bsa.get(x, y + 1, z)
      val upBlock = upState.block

      if (upBlock == Blocks.LILY_PAD || upBlock is CarpetBlock) {
        return true
      }

      if (BlockUtils.isFlowing(x, y, z, state, bsa) || upState.fluidState.type == Fluids.FLOWING_WATER) {
        return BlockUtils.isWater(upState)
      }

      return BlockUtils.isWater(upState) xor false
    }

    return false
  }

}
