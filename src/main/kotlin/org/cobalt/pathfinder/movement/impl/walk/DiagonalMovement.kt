package org.cobalt.pathfinder.movement.impl.walk

import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.movement.*

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

    if (!MovementHelper.canWalkOn(ctx, x, y - 1, z)) {
      return
    }

    if (!MovementHelper.canWalkThrough(ctx, currNode.x + dx, y, currNode.z)) {
      return
    }

    if (!MovementHelper.canWalkThrough(ctx, currNode.x, y, currNode.z + dz)) {
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
