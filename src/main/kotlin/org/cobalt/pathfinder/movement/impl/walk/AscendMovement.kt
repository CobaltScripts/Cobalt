package org.cobalt.pathfinder.movement.impl.walk

import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.movement.*

// TODO: Handle ladder & vine climbing & jump boost
class AscendMovement(
  val dx: Int,
  val dz: Int,
) : Movement(MovementType.WALK) {

  override fun calculateCost(
    ctx: CalculationContext,
    currNode: PathNode,
    res: MovementResult,
  ) {
    val x = currNode.x + dx
    val y = currNode.y + 1
    val z = currNode.z + dz

    if (!MovementHelper.canWalkOn(ctx, x, y - 1, z)) {
      return
    }

    if (!MovementHelper.canWalkThrough(ctx, currNode.x, currNode.y + 1, currNode.z)) {
      return
    }

    res.set(x, y, z)
    res.cost = 1.0
  }

  companion object {
    val NORTH = AscendMovement(0, -1)
    val SOUTH = AscendMovement(0, 1)
    val EAST = AscendMovement(1, 0)
    val WEST = AscendMovement(-1, 0)
  }

}
