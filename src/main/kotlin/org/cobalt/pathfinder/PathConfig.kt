package org.cobalt.pathfinder

import org.cobalt.pathfinder.calculate.PathMode
import org.cobalt.pathfinder.goal.Goal

class PathConfig(
  val goal: Goal,
  val mode: PathMode = PathMode.WALK,
  val shouldSprint: Boolean = true,
  val returnBestNode: Boolean = false,
  val maxCalculationTime: Long = MAX_CALCULATION_TIME,
  val hasReachedThreshold: Double = 0.09,
) {

  val allowFly: Boolean =
    mode == PathMode.FLY

  companion object {
    const val MAX_CALCULATION_TIME = 10_000_000_000L
  }

}
