package org.cobalt.pathfinder.movement

import org.cobalt.pathfinder.calculate.PathNode

// TODO: Implement costs in each movement type (to improve path quality)
abstract class Movement(
  val movementType: MovementType,
) {

  abstract fun calculateCost(ctx: CalculationContext, currNode: PathNode, res: MovementResult)
}
