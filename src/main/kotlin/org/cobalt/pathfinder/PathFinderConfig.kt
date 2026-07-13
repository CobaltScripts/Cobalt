package org.cobalt.pathfinder

object PathFinderConfig {
  var shouldSprint: Boolean = true
  var preferShifting: Boolean = false
  var returnBestNode: Boolean = false
  var maxCalculationTime: Long = 5_000
  var hasReachedThreshold: Double = 0.09 // // = sqrt(0.3), so 0.3 blocks of distance is the threshold
}
