package org.cobalt.pathfinder

object PathFindingConfig {
  var shouldSprint: Boolean = true
  var returnBestNode: Boolean = false
  var maxCalculationTime: Long = 10_000_000_000L
  var hasReachedThreshold: Double = 0.09 // // = sqrt(0.3), so 0.3 blocks of distance is the threshold
}
