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
import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.helper.BlockStateAccessor
import org.cobalt.pathfinder.helper.PlayerInput
import org.cobalt.pathfinder.precompute.Ternary
import org.cobalt.pathfinder.precompute.Ternary.*

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

  // TODO: make it actually good
  @JvmStatic
  fun getRotationTarget(playerPos: Vec3, nodes: List<PathNode>, currentIndex: Int): Vec3 {
    val startIndex = (currentIndex - 1).coerceAtLeast(0)
    var target: Vec3? = null

    // TODO: remove hardcoded lookahead distance and make it a setting
    val lookaheadDistance = 10.0

    for (i in startIndex until nodes.size - 1) {
      val start = nodes[i].centerVec.add(0.0, 1.0, 0.0)
      val end = nodes[i + 1].centerVec.add(0.0, 1.0, 0.0)

      val intersection = findIntersection(playerPos, start, end, lookaheadDistance) ?: continue
      target = intersection
    }

    val lastNode = nodes.last().centerVec.add(0.0, 1.0, 0.0)
    val endDirection = getExtendedEndDirection(nodes, playerPos)
    val virtualEnd = lastNode.add(endDirection.scale(lookaheadDistance))
    val extendedIntersection = findIntersection(playerPos, lastNode, virtualEnd, lookaheadDistance)

    if (extendedIntersection != null) {
      target = extendedIntersection
    }

    if (target != null) {
      return target
    }

    return if (playerPos.distanceTo(virtualEnd) <= lookaheadDistance) {
      virtualEnd
    } else {
      nodes[currentIndex].centerVec.add(0.0, 1.0, 0.0)
    }
  }

  private fun getExtendedEndDirection(nodes: List<PathNode>, playerPos: Vec3): Vec3 {
    val lastNode = nodes.last().centerVec.add(0.0, 1.0, 0.0)

    val reference = if (nodes.size >= 2) {
      nodes[nodes.size - 2].centerVec.add(0.0, 1.0, 0.0)
    } else {
      playerPos
    }

    val direction = lastNode.subtract(reference)

    return if (direction.lengthSqr() > 1.0E-4) {
      direction.normalize()
    } else {
      Vec3(0.0, 0.0, 1.0)
    }
  }

  private fun findIntersection(
    sphereCenter: Vec3,
    start: Vec3,
    end: Vec3,
    sphereRadius: Double
  ): Vec3? {
    val direction = end.subtract(start)
    val offset = start.subtract(sphereCenter)

    val directionLengthSqr = direction.dot(direction)

    if (directionLengthSqr == 0.0) {
      return null
    }

    val linearTerm = 2.0 * offset.dot(direction)
    val constantTerm = offset.dot(offset) - sphereRadius * sphereRadius
    val discriminant = linearTerm * linearTerm - 4.0 * directionLengthSqr * constantTerm

    if (discriminant < 0.0) {
      return null
    }

    val sqrtDiscriminant = sqrt(discriminant)
    val nearIntersection = (-linearTerm - sqrtDiscriminant) / (2.0 * directionLengthSqr)
    val farIntersection = (-linearTerm + sqrtDiscriminant) / (2.0 * directionLengthSqr)

    return when {
      farIntersection in 0.0..1.0 -> {
        start.add(direction.scale(farIntersection))
      }

      nearIntersection in 0.0..1.0 -> {
        start.add(direction.scale(nearIntersection))
      }

      else -> null
    }
  }

}
