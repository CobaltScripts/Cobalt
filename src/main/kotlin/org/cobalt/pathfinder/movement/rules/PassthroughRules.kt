package org.cobalt.pathfinder.movement.rules

import net.minecraft.world.level.block.AbstractSkullBlock
import net.minecraft.world.level.block.AirBlock
import net.minecraft.world.level.block.AmethystClusterBlock
import net.minecraft.world.level.block.AzaleaBlock
import net.minecraft.world.level.block.BaseFireBlock
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.CarpetBlock
import net.minecraft.world.level.block.CauldronBlock
import net.minecraft.world.level.block.DoorBlock
import net.minecraft.world.level.block.FenceGateBlock
import net.minecraft.world.level.block.ShulkerBoxBlock
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.SnowLayerBlock
import net.minecraft.world.level.block.TrapDoorBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.LilyPadBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.material.WaterFluid
import net.minecraft.world.level.pathfinder.PathComputationType
import org.cobalt.pathfinder.helper.BlockStateAccessor
import org.cobalt.pathfinder.movement.rules.StandingRules.canStandOn
import org.cobalt.pathfinder.movement.rules.data.BlockSetRule
import org.cobalt.pathfinder.movement.rules.data.BlockTypeRule
import org.cobalt.pathfinder.movement.rules.data.Ternary
import org.cobalt.util.BlockUtils

object PassthroughRules {

  val PASS_THROUGH_RULES = listOf(
    BlockTypeRule(
      setOf(AirBlock::class.java),
      Ternary.YES,
    ),

    BlockSetRule(
      setOf(
        Blocks.COBWEB,
        Blocks.END_PORTAL,
        Blocks.COCOA,
        Blocks.BUBBLE_COLUMN,
        Blocks.HONEY_BLOCK,
        Blocks.END_ROD,
        Blocks.SWEET_BERRY_BUSH,
        Blocks.POINTED_DRIPSTONE,
        Blocks.BIG_DRIPLEAF,
        Blocks.POWDER_SNOW,
      ),
      Ternary.NO,
    ),

    BlockTypeRule(
      setOf(
        BaseFireBlock::class.java,
        AbstractSkullBlock::class.java,
        ShulkerBoxBlock::class.java,
        SlabBlock::class.java,
        AmethystClusterBlock::class.java,
        AzaleaBlock::class.java,
      ),
      Ternary.NO,
    ),

    BlockTypeRule(
      setOf(
        CarpetBlock::class.java,
        SnowLayerBlock::class.java,
      ),
      Ternary.MAYBE,
    ),

    BlockTypeRule(
      setOf(CauldronBlock::class.java),
      Ternary.NO,
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
    PASS_THROUGH_RULES.firstOrNull { it.matches(state) }
      ?.let { return it.result }

    val block = state.block

    if (block is DoorBlock || block is FenceGateBlock || block is TrapDoorBlock) {
      return Ternary.MAYBE
    }

    if (!state.fluidState.isEmpty) {
      return if (state.fluidState.type.getAmount(state.fluidState) != 8) {
        Ternary.NO
      } else {
        Ternary.MAYBE
      }
    }

    return if (state.isPathfindable(PathComputationType.LAND)) {
      Ternary.YES
    } else {
      Ternary.NO
    }
  }

  fun canPassThrough(bsa: BlockStateAccessor, x: Int, y: Int, z: Int): Boolean {
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
    val block = state.block

    return when {
      block is DoorBlock || block is FenceGateBlock || block is TrapDoorBlock -> {
        state.getValue(BlockStateProperties.OPEN)
      }

      block is CarpetBlock -> {
        canStandOn(bsa, x, y - 1, z)
      }

      block is SnowLayerBlock -> {
        if (!bsa.isLoaded(x, z)) {
          true
        } else if (state.getValue(SnowLayerBlock.LAYERS) >= 3) {
          false
        } else {
          canStandOn(bsa, x, y - 1, z)
        }
      }

      !state.fluidState.isEmpty -> {
        val fluidState = state.fluidState

        if (BlockUtils.isFlowing(x, y, z, state, bsa)) {
          false
        } else {
          val upState = bsa.get(x, y + 1, z)

          if (!upState.fluidState.isEmpty || upState.block is LilyPadBlock) {
            false
          } else {
            fluidState.type is WaterFluid
          }
        }
      }

      else -> state.isPathfindable(PathComputationType.LAND)
    }
  }

}
