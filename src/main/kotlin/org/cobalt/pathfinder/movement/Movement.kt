package org.cobalt.pathfinder.movement

import org.cobalt.pathfinder.PathConfig
import org.cobalt.pathfinder.calculate.PathNode

abstract class Movement(
  val type: MovementType,
) {

  abstract fun calculateCost(ctx: CalculationContext, currNode: PathNode, res: MovementResult)
  abstract fun updateState(config: PathConfig, nodes: List<PathNode>, currNodeIndex: Int): MovementState

}
