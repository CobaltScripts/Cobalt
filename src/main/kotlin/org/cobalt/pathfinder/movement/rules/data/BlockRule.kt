package org.cobalt.pathfinder.movement.rules.data

import net.minecraft.world.level.block.state.BlockState

interface BlockRule {
  val result: Ternary

  fun matches(state: BlockState): Boolean
}
