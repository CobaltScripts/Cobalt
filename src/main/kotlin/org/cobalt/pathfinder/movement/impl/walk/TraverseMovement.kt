package org.cobalt.pathfinder.movement.impl.walk

import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.movement.CalculationContext
import org.cobalt.pathfinder.movement.Movement
import org.cobalt.pathfinder.movement.MovementHelper
import org.cobalt.pathfinder.movement.MovementResult
import org.cobalt.pathfinder.movement.MovementType

class TraverseMovement(
  val dx: Int,
  val dz: Int,
) : Movement(MovementType.WALK) {

  override fun calculateCost(
    ctx: CalculationContext,
    currNode: PathNode,
    res: MovementResult,
  ) {
    val x = currNode.x + dx
    val y = currNode.y
    val z = currNode.z + dz

    if (!MovementHelper.canWalkOn(ctx, x, y - 1, z)) {
      return
    }

    res.set(x, y, z)
    res.cost = 1.0
  }

  companion object {
    val NORTH = TraverseMovement(0, -1)
    val SOUTH = TraverseMovement(0, 1)
    val EAST = TraverseMovement(1, 0)
    val WEST = TraverseMovement(-1, 0)
  }

}
