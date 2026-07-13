package org.cobalt.pathfinder.movement.rules.data

import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

class BlockTypeRule(
    private val types: Set<Class<out Block>>,
    override val result: Ternary,
) : BlockRule {
  override fun matches(state: BlockState): Boolean =
    types.any { it.isInstance(state.block) }
}
