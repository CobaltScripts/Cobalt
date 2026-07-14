package org.cobalt.pathfinder.movement.walk

import org.cobalt.pathfinder.PathConfig
import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.helper.MovementController
import org.cobalt.pathfinder.movement.CalculationContext
import org.cobalt.pathfinder.movement.Movement
import org.cobalt.pathfinder.movement.MovementResult
import org.cobalt.pathfinder.movement.MovementState
import org.cobalt.pathfinder.movement.MovementType
import org.cobalt.pathfinder.movement.MovementValidator

class DescendMovement(
  val dx: Int,
  val dz: Int,
) : Movement(MovementType.WALK) {

  override fun updateState(config: PathConfig, nodes: List<PathNode>, currNodeIndex: Int): MovementState {
    TODO("Not yet implemented")
  }

  override fun calculateCost(
    ctx: CalculationContext,
    currNode: PathNode,
    res: MovementResult,
  ) {
    val x = currNode.x + dx
    val y = currNode.y - 1
    val z = currNode.z + dz

    if (!MovementValidator.canWalkOn(ctx, x, y - 1, z) ||
      !MovementValidator.canWalkThrough(ctx, x, y + 1, z)
    ) {
      return
    }

    res.set(x, y, z)
    res.cost = 1.0
  }

  companion object {
    val NORTH = DescendMovement(0, -1)
    val SOUTH = DescendMovement(0, 1)
    val EAST = DescendMovement(1, 0)
    val WEST = DescendMovement(-1, 0)
  }

}
