package org.cobalt.pathfinder.movement.walk

import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.movement.CalculationContext
import org.cobalt.pathfinder.movement.Movement
import org.cobalt.pathfinder.movement.MovementResult
import org.cobalt.pathfinder.movement.MovementType
import org.cobalt.pathfinder.movement.MovementValidator

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

    if (!MovementValidator.canWalkOn(ctx, x, y - 1, z)) {
      return
    }

    if (!MovementValidator.canWalkThrough(ctx, currNode.x, currNode.y + 1, currNode.z)) {
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
