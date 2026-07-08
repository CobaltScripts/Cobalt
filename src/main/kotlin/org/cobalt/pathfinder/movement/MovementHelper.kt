@file:Suppress("TooManyFunctions")

package org.cobalt.pathfinder.movement

import kotlin.math.sqrt
import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.level.EmptyBlockGetter
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.piston.MovingPistonBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FlowingFluid
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.level.material.WaterFluid
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.phys.Vec3
import org.cobalt.pathfinder.helper.BlockStateAccessor
import org.cobalt.pathfinder.helper.PlayerInput
import org.cobalt.pathfinder.precompute.Ternary
import org.cobalt.pathfinder.precompute.Ternary.*
import org.cobalt.util.RotationUtils.RAD_TO_DEG
import org.cobalt.util.rotation.Rotation

object MovementHelper {

  @JvmStatic
  fun canWalkOn(
    ctx: CalculationContext,
    x: Int, y: Int, z: Int,
    state: BlockState = ctx.bsa.get(x, y, z),
  ): Boolean {
    if (!canWalkThrough(ctx, x, y + 1, z)) {
      return false
    }

    return canStandOn(ctx, x, y, z, state)
  }

  @JvmStatic
  fun canWalkThrough(
    ctx: CalculationContext,
    x: Int, y: Int, z: Int,
    state: BlockState = ctx.bsa.get(x, y, z),
  ): Boolean {
    return canPassThrough(ctx, x, y, z, state) &&
      canPassThrough(ctx, x, y + 1, z)
  }

  @JvmStatic
  fun canPassThrough(
    ctx: CalculationContext,
    x: Int, y: Int, z: Int,
    state: BlockState = ctx.bsa.get(x, y, z),
  ): Boolean {
    return ctx.precomputedData.canPassThrough(ctx, x, y, z, state)
  }

  @JvmStatic
  fun canStandOn(
    ctx: CalculationContext,
    x: Int, y: Int, z: Int,
    state: BlockState = ctx.bsa.get(x, y, z),
  ): Boolean {
    return ctx.precomputedData.canStandOn(ctx, x, y, z, state)
  }

  @JvmStatic
  fun canPassThrough(
    bsa: BlockStateAccessor,
    x: Int, y: Int, z: Int,
    state: BlockState = bsa.get(x, y, z),
  ): Boolean {
    val result = canPassThroughState(state)

    if (result == YES) {
      return true
    }

    if (result == NO) {
      return false
    }

    return canPassThroughPosition(bsa, x, y, z, state)
  }

  @JvmStatic
  fun canStandOn(
    bsa: BlockStateAccessor,
    x: Int, y: Int, z: Int,
    state: BlockState = bsa.get(x, y, z),
  ): Boolean {
    val result = canStandOnState(state)

    if (result == YES) {
      return true
    }

    if (result == NO) {
      return false
    }

    return canStandOnPosition(bsa, x, y, z, state)
  }

  @JvmStatic
  fun canPassThroughState(state: BlockState): Ternary {
    val block = state.block

    return when {
      block is AirBlock -> YES

      block is BaseFireBlock ||
        block == Blocks.COBWEB ||
        block == Blocks.END_PORTAL ||
        block == Blocks.COCOA ||
        block is AbstractSkullBlock ||
        block == Blocks.BUBBLE_COLUMN ||
        block is ShulkerBoxBlock ||
        block is SlabBlock ||
        block is TrapDoorBlock ||
        block == Blocks.HONEY_BLOCK ||
        block == Blocks.END_ROD ||
        block == Blocks.SWEET_BERRY_BUSH ||
        block == Blocks.POINTED_DRIPSTONE ||
        block is AmethystClusterBlock ||
        block is AzaleaBlock ||
        block == Blocks.BIG_DRIPLEAF ||
        block == Blocks.POWDER_SNOW -> NO

      block is DoorBlock || block is FenceGateBlock ->
        if (block == Blocks.IRON_DOOR) NO else YES

      block is CarpetBlock ||
        block is SnowLayerBlock -> MAYBE

      !state.fluidState.isEmpty ->
        if (state.fluidState.type.getAmount(state.fluidState) != 8) NO else MAYBE

      block is CauldronBlock -> NO

      else ->
        if (state.isPathfindable(PathComputationType.LAND)) YES else NO
    }
  }

  @JvmStatic
  fun canStandOnState(state: BlockState): Ternary {
    val block = state.block

    return when {
      isBlockNormalCube(state) &&
        block != Blocks.BUBBLE_COLUMN &&
        block != Blocks.HONEY_BLOCK -> YES

      block is AzaleaBlock ||
        block == Blocks.LADDER ||
        block == Blocks.VINE ||
        block == Blocks.FARMLAND ||
        block == Blocks.DIRT_PATH ||
        block == Blocks.SOUL_SAND ||
        block == Blocks.ENDER_CHEST ||
        block == Blocks.CHEST ||
        block == Blocks.TRAPPED_CHEST ||
        block == Blocks.GLASS ||
        block is StainedGlassBlock ||
        block is StairBlock ||
        block is SlabBlock -> YES

      isWater(state) -> MAYBE
      isLava(state) -> MAYBE

      else -> NO
    }
  }

  private fun canPassThroughPosition(bsa: BlockStateAccessor, x: Int, y: Int, z: Int, state: BlockState): Boolean {
    val block = state.block

    return when {
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

        if (isFlowing(x, y, z, state, bsa)) {
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

  private fun canStandOnPosition(bsa: BlockStateAccessor, x: Int, y: Int, z: Int, state: BlockState): Boolean {
    if (isWater(state)) {
      val upState = bsa.get(x, y + 1, z)
      val upBlock = upState.block

      if (upBlock == Blocks.LILY_PAD || upBlock is CarpetBlock) {
        return true
      }

      if (isFlowing(x, y, z, state, bsa) || upState.fluidState.type == Fluids.FLOWING_WATER) {
        return isWater(upState)
      }

      return isWater(upState) xor false
    }

    return false
  }

  @JvmStatic
  fun isLiquid(state: BlockState): Boolean {
    return !state.fluidState.isEmpty
  }

  @JvmStatic
  fun isWater(state: BlockState): Boolean {
    val fluid = state.fluidState.type
    return fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER
  }

  @JvmStatic
  fun isLava(state: BlockState): Boolean {
    val fluid = state.fluidState.type
    return fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA
  }

  private fun possiblyFlowing(state: BlockState): Boolean {
    val fluidState = state.fluidState

    return fluidState.type is FlowingFluid &&
      fluidState.type.getAmount(fluidState) != 8
  }

  @JvmStatic
  private fun isFlowing(x: Int, y: Int, z: Int, state: BlockState, bsa: BlockStateAccessor): Boolean {
    val fluidState = state.fluidState

    if (fluidState.type !is FlowingFluid) {
      return false
    }

    if (fluidState.type.getAmount(fluidState) != 8) {
      return true
    }

    return possiblyFlowing(bsa.get(x + 1, y, z))
      || possiblyFlowing(bsa.get(x - 1, y, z))
      || possiblyFlowing(bsa.get(x, y, z + 1))
      || possiblyFlowing(bsa.get(x, y, z - 1))
  }

  @JvmStatic
  fun isBlockNormalCube(state: BlockState): Boolean {
    val block = state.block

    if (
      block is BambooStalkBlock ||
      block is MovingPistonBlock ||
      block is ScaffoldingBlock
    ) {
      return false
    }

    if (
      block is ShulkerBoxBlock ||
      block is PointedDripstoneBlock ||
      block is AmethystClusterBlock
    ) {
      return false
    }

    return try {
      Block.isShapeFullBlock(state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO))
    } catch (_: Exception) {
      false
    }
  }

  @JvmStatic
  fun getRotation(orig: Vec3, dest: Vec3): Rotation {
    val delta = doubleArrayOf(orig.x - dest.x, orig.y - dest.y, orig.z - dest.z)
    val yaw = Mth.atan2(delta[0], -delta[2])
    val dist = sqrt(delta[0] * delta[0] + delta[2] * delta[2])
    val pitch = Mth.atan2(delta[1], dist)

    return Rotation(
      (yaw * RAD_TO_DEG).toFloat(),
      (pitch * RAD_TO_DEG).toFloat()
    )
  }

  @JvmStatic
  fun getNeededKeys(playerYaw: Float, idealYaw: Float): PlayerInput {
    val diff = Mth.wrapDegrees(idealYaw - playerYaw)

    return when {
      diff >= -22.5f && diff < 22.5f -> PlayerInput(forward = true)
      diff in 22.5f..<67.5f -> PlayerInput(forward = true, right = true)
      diff in 67.5f..<112.5f -> PlayerInput(right = true)
      diff in 112.5f..<157.5f -> PlayerInput(backward = true, right = true)
      diff >= 157.5f || diff < -157.5f -> PlayerInput(backward = true)
      diff >= -157.5f && diff < -112.5f -> PlayerInput(backward = true, left = true)
      diff >= -112.5f && diff < -67.5f -> PlayerInput(left = true)
      else -> PlayerInput(forward = true, left = true)
    }
  }

}
