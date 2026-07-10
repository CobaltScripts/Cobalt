package org.cobalt.pathfinder.goal

import kotlin.math.abs

class GoalBlock(
  val goalX: Int,
  val goalY: Int,
  val goalZ: Int,
) : Goal {

  override fun heuristic(x: Int, y: Int, z: Int): Double {
    val dx = abs(goalX - x).toDouble()
    val dy = abs(goalY - y) * 3.0
    val dz = abs(goalZ - z).toDouble()

    return dx + dy + dz
  }

  override fun isAtGoal(x: Int, y: Int, z: Int): Boolean {
    return goalX == x && goalY == y && goalZ == z
  }

}
