package org.cobalt.api.event.impl.client

import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import org.cobalt.api.event.Event

@Suppress("UNUSED_PARAMETER")
class BlockChangeEvent(
  val pos: BlockPos,
  val oldBlock: BlockState,
  val newBlock: BlockState,
) : Event(false)
