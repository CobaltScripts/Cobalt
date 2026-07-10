package org.cobalt.pathfinder.goal

interface Goal {

  fun heuristic(x: Int, y: Int, z: Int): Double
  fun isAtGoal(x: Int, y: Int, z: Int): Boolean

}
