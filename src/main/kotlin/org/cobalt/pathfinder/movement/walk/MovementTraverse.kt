package org.cobalt.pathfinder.movement.walk

import org.cobalt.pathfinder.PathConfig
import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.movement.CalculationContext
import org.cobalt.pathfinder.movement.Movement
import org.cobalt.pathfinder.movement.MovementResult
import org.cobalt.pathfinder.movement.MovementState
import org.cobalt.pathfinder.movement.MovementType
import org.cobalt.pathfinder.movement.MovementValidator
import org.cobalt.util.client.PlayerUtils
import org.cobalt.pathfinder.movement.MovementTarget
import org.cobalt.pathfinder.movement.MovementStatus

class MovementTraverse(
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
    val y = currNode.y
    val z = currNode.z + dz

    if (!MovementValidator.canWalkOn(ctx, x, y, z)) {
      return
    }

    res.set(x, y, z)
    res.cost = ctx.costs.oneBlockWalkCost
  }

  companion object {
    val NORTH = MovementTraverse(0, -1)
    val SOUTH = MovementTraverse(0, 1)
    val EAST = MovementTraverse(1, 0)
    val WEST = MovementTraverse(-1, 0)
  }

}
