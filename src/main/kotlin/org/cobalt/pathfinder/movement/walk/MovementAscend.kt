package org.cobalt.pathfinder.movement.walk

import org.cobalt.pathfinder.PathConfig
import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.movement.CalculationContext
import org.cobalt.pathfinder.movement.Movement
import org.cobalt.pathfinder.movement.MovementResult
import org.cobalt.pathfinder.movement.MovementState
import org.cobalt.pathfinder.movement.MovementType
import org.cobalt.pathfinder.movement.MovementValidator
import kotlin.math.max
import kotlin.math.ceil
import org.cobalt.pathfinder.movement.MovementStatus
import org.cobalt.pathfinder.movement.MovementTarget
import org.cobalt.util.client.PlayerUtils

class MovementAscend(
  val dx: Int,
  val dz: Int,
) : Movement(MovementType.WALK) {

  override fun updateState(config: PathConfig, nodes: List<PathNode>, currNodeIndex: Int): MovementState {
    val targetNode = nodes[currNodeIndex]

    if (PlayerUtils.blockStandingOn == targetNode.block) {
      return MovementState(status = MovementStatus.REACHED)
    }

    return MovementState(MovementTarget())
  }

  override fun calculateCost(
    ctx: CalculationContext,
    currNode: PathNode,
    res: MovementResult,
  ) {
    val x = currNode.x + dx
    val z = currNode.z + dz

    var y = currNode.y + 1
    var landingY: Int? = null

    while (y <= currNode.y + ctx.maxJumpBlock) {
      if (!MovementValidator.canWalkThrough(ctx, currNode.x, y + 1, currNode.z)) {
        break
      }

      if (
        MovementValidator.canWalkOn(ctx, x, y, z) &&
        MovementValidator.canWalkThrough(ctx, x, y + 1, z)
      ) {
        landingY = y
        break
      }

      y++
    }

    if (landingY == null) {
      return
    }

    val jumpHeight = landingY - currNode.y
    res.set(x, landingY, z)
    res.cost = ctx.costs.oneBlockWalkCost + ctx.costs.jumpOneBlockCost * jumpHeight
  }

  companion object {
    val NORTH = MovementAscend(0, -1)
    val SOUTH = MovementAscend(0, 1)
    val EAST = MovementAscend(1, 0)
    val WEST = MovementAscend(-1, 0)
  }

}
