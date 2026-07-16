package org.cobalt.pathfinder.movement.walk

import org.cobalt.pathfinder.PathConfig
import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.movement.CalculationContext
import org.cobalt.pathfinder.movement.Movement
import org.cobalt.pathfinder.movement.MovementResult
import org.cobalt.pathfinder.movement.MovementState
import org.cobalt.pathfinder.movement.MovementType
import org.cobalt.pathfinder.movement.MovementValidator
import kotlin.math.sqrt
import org.cobalt.util.client.PlayerUtils
import org.cobalt.pathfinder.movement.MovementTarget
import org.cobalt.pathfinder.movement.MovementStatus

class MovementDiagonal(
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

    if (
      !MovementValidator.canWalkOn(ctx, x, y, z) ||
      !MovementValidator.canWalkThrough(ctx, x, y + 1, currNode.z) ||
      !MovementValidator.canWalkThrough(ctx, currNode.x, y + 1, z)
    ) {
      return
    }

    res.set(x, y, z)
    res.cost = ctx.costs.oneBlockWalkCost * SQRT_2
  }

  companion object {
    private val SQRT_2 = sqrt(2.0)

    val NORTH_EAST = MovementDiagonal(1, -1)
    val NORTH_WEST = MovementDiagonal(-1, -1)
    val SOUTH_EAST = MovementDiagonal(1, 1)
    val SOUTH_WEST = MovementDiagonal(-1, 1)
  }

}
