package org.cobalt.pathfinder.movement.fly

import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.movement.*
import org.cobalt.pathfinder.movement.rules.BlockTraversalRules

class FlyDescendMovement : Movement(MovementType.FLY) {

  override fun calculateCost(
    ctx: CalculationContext,
    currNode: PathNode,
    res: MovementResult,
  ) {
    val x = currNode.x
    val y = currNode.y - 1
    val z = currNode.z

    if (!BlockTraversalRules.canWalkThrough(ctx, x, y, z)) {
      return
    }

    res.set(x, y, z)
    res.cost = 1.0
  }

  companion object {
    val DEFAULT = FlyDescendMovement()
  }

}
