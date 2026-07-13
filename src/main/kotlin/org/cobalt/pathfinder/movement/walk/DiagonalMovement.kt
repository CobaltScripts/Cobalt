package org.cobalt.pathfinder.movement.walk

import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.movement.*
import org.cobalt.pathfinder.movement.rules.BlockTraversalRules

class DiagonalMovement(
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

    if (!BlockTraversalRules.canWalkThrough(ctx, currNode.x + dx, y, currNode.z)) {
      return
    }

    if (!BlockTraversalRules.canWalkThrough(ctx, currNode.x, y, currNode.z + dz)) {
      return
    }

    res.set(x, y, z)
    res.cost = 1.0
  }

  companion object {
    val NORTH_EAST = DiagonalMovement(1, -1)
    val NORTH_WEST = DiagonalMovement(-1, -1)
    val SOUTH_EAST = DiagonalMovement(1, 1)
    val SOUTH_WEST = DiagonalMovement(-1, 1)
  }

}
