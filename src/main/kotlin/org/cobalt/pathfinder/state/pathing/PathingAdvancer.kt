package org.cobalt.pathfinder.state.pathing

import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import org.cobalt.dsl.centerVec
import org.cobalt.Cobalt
import org.cobalt.pathfinder.PathFindingConfig
import org.cobalt.pathfinder.PathFindingFacade
import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.movement.rules.BlockSupportRules
import org.cobalt.util.PlayerUtils

class PathingAdvancer {
  fun advanceIfReached(playerPos: BlockPos, nodes: List<PathNode>): Boolean {
    val currentIndex = PathFindingFacade.pathIndex

    if (!hasReached(playerPos, nodes, currentIndex)) {
      return false
    }

    while (
      PathFindingFacade.pathIndex + 1 < nodes.size &&
      hasReached(playerPos, nodes, PathFindingFacade.pathIndex)
    ) {
      PathFindingFacade.pathIndex++
    }

    if (PathFindingFacade.pathIndex + 1 >= nodes.size) {
      PathFindingFacade.stop()
    }

    return true
  }

  private fun hasReached(
      playerPos: BlockPos,
      nodes: List<PathNode>,
      currentIndex: Int,
  ): Boolean {
    val node = nodes[currentIndex]

    val nodeCenter = node.centerVec
    val playerVec = playerPos.centerVec()

    if (playerVec.distanceToSqr(nodeCenter) < PathFindingConfig.hasReachedThreshold) return true

    if (currentIndex + 1 >= nodes.size || !canAdvancePastNode(playerPos, node)) {
      return false
    }

    if (!isCloseEnoughToSegment(nodeCenter, playerVec, nodes[currentIndex + 1].centerVec)) {
      return false
    }

    return true
  }

  private fun canAdvancePastNode(playerPos: BlockPos, node: PathNode): Boolean {
    val level = Cobalt.minecraft.level
    requireNotNull(level)

    return BlockSupportRules.isBottomSlab(level.getBlockState(node.blockStandingOn)) ||
      (node.block.y <= playerPos.y && PlayerUtils.onGround)
  }

  private fun isCloseEnoughToSegment(
    nodeCenter: Vec3,
    playerVec: Vec3,
    nextNodeCenter: Vec3,
  ): Boolean {
    val segment = nextNodeCenter.subtract(nodeCenter)
    val toPlayer = playerVec.subtract(nodeCenter)

    if (toPlayer.dot(segment) < 0.0) {
      return false
    }

    val squaredPerpendicularDistance = toPlayer.cross(segment).lengthSqr() / segment.lengthSqr()
    return squaredPerpendicularDistance < 1.0
  }
}


