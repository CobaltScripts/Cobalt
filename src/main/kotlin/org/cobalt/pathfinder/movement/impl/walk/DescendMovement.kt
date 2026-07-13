package org.cobalt.pathfinder.movement.impl.walk

import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.movement.*

// TODO: Handle ladder & vine climbing downwards & falling down multiple blocks
class DescendMovement(
  val dx: Int,
  val dz: Int,
) : Movement(MovementType.WALK) {

  override fun calculateCost(
    ctx: CalculationContext,
    currNode: PathNode,
    res: MovementResult,
  ) {
    val x = currNode.x + dx
    val y = currNode.y - 1
    val z = currNode.z + dz

    if (!MovementHelper.canWalkThrough(ctx, x, currNode.y, z)) {
      return
    }

    if (!MovementHelper.canWalkOn(ctx, x, y - 1, z)) {
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
