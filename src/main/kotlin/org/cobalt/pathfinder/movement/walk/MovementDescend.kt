package org.cobalt.pathfinder.movement.walk

import org.cobalt.pathfinder.PathConfig
import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.movement.CalculationContext
import org.cobalt.pathfinder.movement.Movement
import org.cobalt.pathfinder.movement.MovementResult
import org.cobalt.pathfinder.movement.MovementState
import org.cobalt.pathfinder.movement.MovementType
import org.cobalt.pathfinder.movement.MovementValidator
import org.cobalt.util.block.BlockUtils
import org.cobalt.util.client.PlayerUtils
import org.cobalt.pathfinder.movement.MovementTarget
import org.cobalt.pathfinder.movement.MovementStatus
import kotlin.math.min

class MovementDescend(
  val dx: Int,
  val dz: Int,
) : Movement(MovementType.WALK) {

  override fun updateState(config: PathConfig, nodes: List<PathNode>, currNodeIndex: Int): MovementState {
    val targetNode = nodes[currNodeIndex]

    if (PlayerUtils.position == targetNode.block) {
      return MovementState(status = MovementStatus.REACHED)
    }

    return MovementState(MovementTarget(lookAt = targetNode.centerVec))
  }

  override fun calculateCost(
    ctx: CalculationContext,
    currNode: PathNode,
    res: MovementResult,
  ) {
    val x = currNode.x + dx
    val z = currNode.z + dz

    if (!MovementValidator.canWalkThrough(ctx, x, currNode.y, z)) {
      return
    }

    var y = currNode.y - 1
    var landingY: Int? = null

    while (currNode.y - y <= ctx.maxFallDistance) {
      if (!MovementValidator.canWalkThrough(ctx, x, y, z)) {
        break
      }

      if (MovementValidator.canWalkOn(ctx, x, y - 1, z)) {
        landingY = y
        break
      }

      y--
    }

    if (landingY == null) {
      return
    }

    val sourceHeight = BlockUtils.getCollisionHeight(ctx.bsa, currNode.x, currNode.y - 1, currNode.z)
    val destHeight = BlockUtils.getCollisionHeight(ctx.bsa, x, landingY - 1, z)
    val fallBlocks = (currNode.y - landingY).coerceAtLeast(1)
    val diff = fallBlocks.toDouble() + destHeight - sourceHeight
    val fallDistance = min(ctx.costs.blockFallCost.lastIndex, fallBlocks)

    res.set(x, landingY, z)

    val fallCost = ctx.costs.blockFallCost[fallDistance] + ctx.costs.walkOffOneBlockCost + ctx.costs.centerAfterFallCost

    res.cost = when {
      diff <= 0.5 -> ctx.costs.oneBlockWalkCost
      diff <= ctx.costs.blockFallCost.lastIndex -> fallCost
      else -> ctx.costs.infCost
    }
  }

  companion object {
    val NORTH = MovementDescend(0, -1)
    val SOUTH = MovementDescend(0, 1)
    val EAST = MovementDescend(1, 0)
    val WEST = MovementDescend(-1, 0)
  }

}
