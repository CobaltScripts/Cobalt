package org.cobalt.pathfinder.movement.fly

import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.movement.*
import org.cobalt.pathfinder.movement.rules.BlockTraversalRules

class FlyTraverseMovement(
  val dx: Int,
  val dz: Int,
) : Movement(MovementType.FLY) {

  override fun calculateCost(
    ctx: CalculationContext,
    currNode: PathNode,
    res: MovementResult,
  ) {
    val x = currNode.x + dx
    val y = currNode.y
    val z = currNode.z + dz

    if (!BlockTraversalRules.canWalkThrough(ctx, x, y, z)) {
      return
    }

    res.set(x, y, z)
    res.cost = 1.0
  }

  companion object {
    val NORTH = FlyTraverseMovement(0, -1)
    val SOUTH = FlyTraverseMovement(0, 1)
    val EAST = FlyTraverseMovement(1, 0)
    val WEST = FlyTraverseMovement(-1, 0)
  }

}
