package org.cobalt.pathfinder.movement.walk

import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.movement.*
import org.cobalt.pathfinder.movement.rules.BlockTraversalRules

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

    if (!BlockTraversalRules.canWalkOn(ctx, x, y - 1, z)) {
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
