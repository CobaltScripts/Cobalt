package org.cobalt.pathfinder

import org.cobalt.pathfinder.calculate.PathMode
import org.cobalt.pathfinder.goal.Goal

class PathConfig(
  val goal: Goal,
  val mode: PathMode = PathMode.WALK,
  val returnBestNode: Boolean = false,
  val maxCalculationTime: Long = 10_000_000_000L,
) {

  val allowFly: Boolean =
    mode == PathMode.FLY

}
