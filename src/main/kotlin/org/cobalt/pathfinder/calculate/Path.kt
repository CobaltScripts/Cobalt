package org.cobalt.pathfinder.calculate

import kotlin.time.Duration
import org.cobalt.pathfinder.goal.Goal

data class Path(
  val nodes: List<PathNode>,
  val timeElapsed: Duration,
  val goal: Goal,
)
