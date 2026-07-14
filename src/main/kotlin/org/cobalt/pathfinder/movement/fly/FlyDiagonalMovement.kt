package org.cobalt.pathfinder.movement.fly

import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.movement.CalculationContext
import org.cobalt.pathfinder.movement.Movement
import org.cobalt.pathfinder.movement.MovementResult
import org.cobalt.pathfinder.movement.MovementType
import org.cobalt.pathfinder.movement.MovementValidator

class FlyDiagonalMovement(
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

    if (!MovementValidator.canWalkThrough(ctx, x, y, z)) {
      return
    }

    if (!MovementValidator.canWalkThrough(ctx, currNode.x + dx, y, currNode.z)) {
      return
    }

    if (!MovementValidator.canWalkThrough(ctx, currNode.x, y, currNode.z + dz)) {
      return
    }

    res.set(x, y, z)
    res.cost = 1.0
  }

  companion object {
    val NORTH_EAST = FlyDiagonalMovement(1, -1)
    val NORTH_WEST = FlyDiagonalMovement(-1, -1)
    val SOUTH_EAST = FlyDiagonalMovement(1, 1)
    val SOUTH_WEST = FlyDiagonalMovement(-1, 1)
  }

}
