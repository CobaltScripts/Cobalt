package org.cobalt.pathfinder.movement

import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FlowingFluid
import org.cobalt.pathfinder.helper.BlockStateAccessor

object FlowingCheck {
  private fun possiblyFlowing(state: BlockState): Boolean {
    val fluidState = state.fluidState

    return fluidState.type is FlowingFluid &&
      fluidState.type.getAmount(fluidState) != 8
  }

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

}
