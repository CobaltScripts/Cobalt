package org.cobalt.pathfinder.helper

import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.level.chunk.status.ChunkStatus

class BlockStateAccessor(val level: ClientLevel) {

  val access: BlockViewWrapper = BlockViewWrapper(this)
  val mutableBlockPos: BlockPos.MutableBlockPos = BlockPos.MutableBlockPos()

  private val provider = level.chunkSource
  private var prevChunk: LevelChunk? = null
  private val air = Blocks.AIR.defaultBlockState()

  fun get(x: Int, y: Int, z: Int): BlockState {
    val y0 = y - level.dimensionType().minY()

    if (y0 !in 0 until level.dimensionType().height()) {
      return air
    }

    // TODO: Cache world chunks and fetch block state (prob time to convert to a better system utilizing enums)

    prevChunk?.takeIf { it.pos.x == x shr 4 && it.pos.z == z shr 4 }
      ?.let { return getFromChunk(it, x, y0, z) }

    val chunk = provider.getChunk(x shr 4, z shr 4, ChunkStatus.FULL, false)
      ?.takeIf { !it.isEmpty }
      ?: return air

    prevChunk = chunk
    return getFromChunk(chunk, x, y0, z)
  }

  fun isLoaded(x: Int, z: Int): Boolean {
    prevChunk?.takeIf { it.pos.x == x shr 4 && it.pos.z == z shr 4 }
      ?.let { return true }

    val chunk = provider.getChunk(x shr 4, z shr 4, ChunkStatus.FULL, false)
      ?.takeIf { !it.isEmpty } ?: return false

    prevChunk = chunk
    return true
  }

  fun getFromChunk(chunk: LevelChunk, x: Int, y: Int, z: Int): BlockState {
    val section = chunk.getSections()[y shr 4]

    if (section.hasOnlyAir()) {
      return air
    }

    return section.getBlockState(x and 15, y and 15, z and 15)
  }

}
