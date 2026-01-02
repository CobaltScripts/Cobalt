package org.cobalt.api.util

import kotlin.math.ceil
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.util.math.BlockPos
import org.cobalt.Cobalt.mc

object PlayerUtils {

  /**
  * @return The player's current position
  */
  val position: BlockPos?
    get() = mc.player?.position()

 /**
  * @return The player's current FOV
  */
  val fov: Int
    get() = mc.options.fov.value

  /**
   * @return The current position of a ClientPlayerEntity
   */
  fun ClientPlayerEntity.position(): BlockPos {
    return BlockPos.ofFloored(x, ceil(y - 0.25), z)
  }

}
