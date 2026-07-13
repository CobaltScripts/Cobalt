package org.cobalt.pathfinder.movement.impl.fly

import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.movement.CalculationContext
import org.cobalt.pathfinder.movement.Movement
import org.cobalt.pathfinder.movement.MovementHelper
import org.cobalt.pathfinder.movement.MovementResult
import org.cobalt.pathfinder.movement.MovementType

class FlyAscendMovement : Movement(MovementType.FLY) {

  override fun calculateCost(
    ctx: CalculationContext,
    currNode: PathNode,
    res: MovementResult,
  ) {
    val x = currNode.x
    val y = currNode.y + 1
    val z = currNode.z

    if (!MovementHelper.canWalkThrough(ctx, x, y, z)) {
      return
    }

    res.set(x, y, z)
    res.cost = 1.0
  }

  companion object {
    val DEFAULT = FlyAscendMovement()
  }

}
