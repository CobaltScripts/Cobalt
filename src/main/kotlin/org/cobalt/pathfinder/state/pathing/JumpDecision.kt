package org.cobalt.pathfinder.state.pathing

import net.minecraft.core.BlockPos
import org.cobalt.Cobalt
import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.movement.rules.BlockSupportRules
import org.cobalt.util.PlayerUtils
import org.cobalt.dsl.centerVec
import org.cobalt.util.helper.Clock
import kotlin.math.abs
import kotlin.random.Random

class JumpDecision {
  private val jumpDelay = Clock()

  fun shouldJump(playerPos: BlockPos, nodes: List<PathNode>, currentIndex: Int): Boolean {
    val level = Cobalt.minecraft.level
    requireNotNull(level)

    if (!PlayerUtils.onGround || !jumpDelay.passed()) {
      return false
    }

    val node = nodes[currentIndex]

    if (node.block.y - playerPos.y < 1 ||
      BlockSupportRules.isBottomSlab(level.getBlockState(node.blockStandingOn))
    ) {
      return false
    }

    val nodeCenter = node.centerVec
    val playerVec = playerPos.centerVec()
    val dx = abs(nodeCenter.x - playerVec.x)
    val dz = abs(nodeCenter.z - playerVec.z)

    if (dx + dz > 1.2 || minOf(dx, dz) > 0.2) {
      return false
    }

    if (PlayerUtils.canFly) {
      jumpDelay.schedule(Random.nextLong(350, 450))
    }

    return true
  }
}
