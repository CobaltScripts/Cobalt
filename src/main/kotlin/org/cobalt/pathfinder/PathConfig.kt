package org.cobalt.pathfinder

import org.cobalt.pathfinder.calculate.PathMode
import org.cobalt.pathfinder.goal.Goal
import org.cobalt.pathfinder.movement.CalculationContext

class PathConfig(
  val goal: Goal,
  val calculationContext: CalculationContext,
  val mode: PathMode = PathMode.WALK,
  val returnBestNode: Boolean = false,
  val maxCalculationTime: Long = 10_000_000_000L,
) {

  val allowFly: Boolean =
    mode == PathMode.FLY

}
