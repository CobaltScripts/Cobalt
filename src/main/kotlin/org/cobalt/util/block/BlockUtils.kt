package org.cobalt.util.block

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.EmptyBlockGetter
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.piston.MovingPistonBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.SlabType
import net.minecraft.world.level.material.FlowingFluid
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.cobalt.pathfinder.helper.BlockStateAccessor


object BlockUtils {

  @JvmStatic
  fun getCollisionHeight(bsa: BlockStateAccessor, x: Int, y: Int, z: Int): Double {
    val state = bsa.get(x, y, z)
    val shape = state.getCollisionShape(bsa.level, bsa.mutableBlockPos.set(x, y, z))

    if (shape.isEmpty) {
      return 0.0
    }

    return shape.max(Direction.Axis.Y)
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
  fun isFlowing(x: Int, y: Int, z: Int, state: BlockState, bsa: BlockStateAccessor): Boolean {
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
  fun possiblyFlowing(state: BlockState): Boolean {
    val fluidState = state.fluidState

    return fluidState.type is FlowingFluid &&
      fluidState.type.getAmount(fluidState) != 8
  }

}
