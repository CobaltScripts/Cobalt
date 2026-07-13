package org.cobalt.pathfinder.movement.rules.data

import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

class BlockSetRule(
  private val blocks: Set<Block>,
  override val result: Ternary,
) : BlockRule {
  override fun matches(state: BlockState): Boolean =
    state.block in blocks
}
