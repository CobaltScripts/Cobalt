@file:Suppress("TooManyFunctions")

package org.cobalt.pathfinder.movement

import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.level.EmptyBlockGetter
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.piston.MovingPistonBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.SlabType
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
  private const val DIRECTIONS_COUNT = 8
  private const val FULL_CIRCLE_DEGREES = 360f
  private const val SECTOR_WIDTH_DEGREES = FULL_CIRCLE_DEGREES / DIRECTIONS_COUNT
  private const val SECTOR_HALF_WIDTH_DEGREES = SECTOR_WIDTH_DEGREES / 2f

  private const val DEAD_BAND_DEGREES = 0.05f

  private const val FORWARD_RIGHT_START_DEGREES = SECTOR_HALF_WIDTH_DEGREES
  private const val RIGHT_START_DEGREES = FORWARD_RIGHT_START_DEGREES + SECTOR_WIDTH_DEGREES
  private const val BACK_RIGHT_START_DEGREES = RIGHT_START_DEGREES + SECTOR_WIDTH_DEGREES
  private const val BACKWARD_START_DEGREES = BACK_RIGHT_START_DEGREES + SECTOR_WIDTH_DEGREES

  private const val FORWARD_LEFT_START_DEGREES = -FORWARD_RIGHT_START_DEGREES
  private const val LEFT_START_DEGREES = -RIGHT_START_DEGREES
  private const val BACK_LEFT_START_DEGREES = -BACK_RIGHT_START_DEGREES
  private const val BACKWARD_NEG_START_DEGREES = -BACKWARD_START_DEGREES

  private const val FORWARD_RIGHT_HOLD_DEGREES = FORWARD_RIGHT_START_DEGREES + DEAD_BAND_DEGREES
  private const val RIGHT_HOLD_DEGREES = RIGHT_START_DEGREES + DEAD_BAND_DEGREES
  private const val BACK_RIGHT_HOLD_DEGREES = BACK_RIGHT_START_DEGREES + DEAD_BAND_DEGREES
  private const val BACKWARD_HOLD_DEGREES = BACKWARD_START_DEGREES + DEAD_BAND_DEGREES

  private const val FORWARD_LEFT_HOLD_DEGREES = FORWARD_LEFT_START_DEGREES - DEAD_BAND_DEGREES
  private const val LEFT_HOLD_DEGREES = LEFT_START_DEGREES - DEAD_BAND_DEGREES
  private const val BACK_LEFT_HOLD_DEGREES = BACK_LEFT_START_DEGREES - DEAD_BAND_DEGREES
  private const val BACKWARD_NEG_HOLD_DEGREES = BACKWARD_NEG_START_DEGREES - DEAD_BAND_DEGREES

  private enum class Sector { FORWARD, FORWARD_RIGHT, RIGHT, BACK_RIGHT, BACKWARD, BACK_LEFT, LEFT, FORWARD_LEFT }

  private var lastSector: Sector = Sector.FORWARD

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
  fun isBottomSlab(state: BlockState): Boolean {
    return state.block is SlabBlock &&
      state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM
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

    if (staysInSector(lastSector, diff)) {
      return sectorToInput(lastSector)
    }

    val newSector = classify(diff)
    lastSector = newSector
    return sectorToInput(newSector)
  }

  private fun staysInSector(sector: Sector, diff: Float): Boolean = when (sector) {
    Sector.FORWARD -> diff in FORWARD_LEFT_HOLD_DEGREES..<FORWARD_RIGHT_HOLD_DEGREES
    Sector.FORWARD_RIGHT -> diff >= FORWARD_RIGHT_START_DEGREES - DEAD_BAND_DEGREES && diff < RIGHT_HOLD_DEGREES
    Sector.RIGHT -> diff >= RIGHT_START_DEGREES - DEAD_BAND_DEGREES && diff < BACK_RIGHT_HOLD_DEGREES
    Sector.BACK_RIGHT -> diff >= BACK_RIGHT_START_DEGREES - DEAD_BAND_DEGREES && diff < BACKWARD_HOLD_DEGREES
    Sector.BACKWARD -> diff >= BACKWARD_START_DEGREES - DEAD_BAND_DEGREES || diff < BACKWARD_NEG_HOLD_DEGREES
    Sector.BACK_LEFT -> diff >= BACKWARD_NEG_START_DEGREES - DEAD_BAND_DEGREES && diff < BACK_LEFT_HOLD_DEGREES
    Sector.LEFT -> diff >= BACK_LEFT_START_DEGREES - DEAD_BAND_DEGREES && diff < LEFT_HOLD_DEGREES
    Sector.FORWARD_LEFT -> diff >= LEFT_START_DEGREES - DEAD_BAND_DEGREES && diff < FORWARD_LEFT_HOLD_DEGREES
  }

  private fun classify(diff: Float): Sector = when {
    diff in FORWARD_LEFT_START_DEGREES..<FORWARD_RIGHT_START_DEGREES -> Sector.FORWARD
    diff in FORWARD_RIGHT_START_DEGREES..<RIGHT_START_DEGREES -> Sector.FORWARD_RIGHT
    diff in RIGHT_START_DEGREES..<BACK_RIGHT_START_DEGREES -> Sector.RIGHT
    diff in BACK_RIGHT_START_DEGREES..<BACKWARD_START_DEGREES -> Sector.BACK_RIGHT
    diff !in BACKWARD_NEG_START_DEGREES..<BACKWARD_START_DEGREES -> Sector.BACKWARD
    diff in BACKWARD_NEG_START_DEGREES..<BACK_LEFT_START_DEGREES -> Sector.BACK_LEFT
    diff in BACK_LEFT_START_DEGREES..<LEFT_START_DEGREES -> Sector.LEFT
    else -> Sector.FORWARD_LEFT
  }

  private fun sectorToInput(sector: Sector): PlayerInput = when (sector) {
    Sector.FORWARD -> PlayerInput(forward = true)
    Sector.FORWARD_RIGHT -> PlayerInput(forward = true, right = true)
    Sector.RIGHT -> PlayerInput(right = true)
    Sector.BACK_RIGHT -> PlayerInput(backward = true, right = true)
    Sector.BACKWARD -> PlayerInput(backward = true)
    Sector.BACK_LEFT -> PlayerInput(backward = true, left = true)
    Sector.LEFT -> PlayerInput(left = true)
    Sector.FORWARD_LEFT -> PlayerInput(forward = true, left = true)
  }

  @JvmStatic
  fun getRotationTarget(playerEyePos: Vec3, nodes: List<PathNode>, currentIndex: Int, lookAhead: Double = 3.0): Vec3 {
    val playerXZ = Vec3(playerEyePos.x, 0.0, playerEyePos.z)

    var bestDistSq = Double.MAX_VALUE
    var bestSegIndex = currentIndex
    var bestT = 0.0

    val searchStart = (currentIndex - 1).coerceAtLeast(0)
    val searchEnd = (currentIndex + 3).coerceAtMost(nodes.size - 1)

    for (i in searchStart until searchEnd) {
      val from = nodes[i].centerVec
      val to = nodes[i + 1].centerVec
      val seg = Vec3(to.x - from.x, 0.0, to.z - from.z)
      val segLenSq = seg.x * seg.x + seg.z * seg.z

      if (segLenSq <= 0.0) continue

      val t = ((playerXZ.x - from.x) * seg.x + (playerXZ.z - from.z) * seg.z)
        .coerceIn(0.0, segLenSq) / segLenSq

      val px = from.x + seg.x * t
      val pz = from.z + seg.z * t
      val dx = playerXZ.x - px
      val dz = playerXZ.z - pz
      val distSq = dx * dx + dz * dz

      if (distSq < bestDistSq) {
        bestDistSq = distSq
        bestSegIndex = i
        bestT = t
      }
    }

    var remaining = lookAhead

    val from = nodes[bestSegIndex].centerVec
    val to = nodes[bestSegIndex + 1].centerVec
    val seg = Vec3(to.x - from.x, 0.0, to.z - from.z)
    val segLen = seg.length()

    if (segLen > 0.0) {
      val remainingSeg = (1.0 - bestT) * segLen
      if (remaining <= remainingSeg) {
        val point = from.add(seg.scale(bestT + remaining / segLen))
        return Vec3(point.x, playerEyePos.y, point.z)
      }
      remaining -= remainingSeg
    }

    for (i in (bestSegIndex + 1) until nodes.size - 1) {
      val sFrom = nodes[i].centerVec
      val sTo = nodes[i + 1].centerVec
      val sSeg = Vec3(sTo.x - sFrom.x, 0.0, sTo.z - sFrom.z)
      val sLen = sSeg.length()

      if (sLen <= 0.0) continue

      if (remaining <= sLen) {
        val point = sFrom.add(sSeg.scale(remaining / sLen))
        return Vec3(point.x, playerEyePos.y, point.z)
      }

      remaining -= sLen
    }

    val last = nodes.last()
    val prev = if (nodes.size > 1) nodes[nodes.size - 2] else last
    val dir = Vec3((last.x - prev.x).toDouble(), 0.0, (last.z - prev.z).toDouble())
    val dirLen = dir.length()
    val extrapolated = if (dirLen > 0.0) {
      last.centerVec.add(dir.scale(remaining / dirLen))
    } else {
      last.centerVec
    }

    return Vec3(extrapolated.x, playerEyePos.y, extrapolated.z)
  }

}
