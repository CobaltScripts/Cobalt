package org.cobalt.pathfinder.goal

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import org.cobalt.pathfinder.movement.CalculationContext

class GoalBlock(
  val goalX: Int,
  val goalY: Int,
  val goalZ: Int,
  val ctx: CalculationContext
) : Goal {

  override fun heuristic(x: Int, y: Int, z: Int): Double {
    val dx = abs(goalX - x)
    val dy = abs(goalY - y)
    val dz = abs(goalZ - z)

    val diagonal = min(dx, dz).toDouble()
    val straight = max(dx, dz).toDouble() - diagonal

    val horizontal = straight + diagonal * SQRT_2
    val vertical = dy * 2.0

    return horizontal + vertical
  }

  override fun isAtGoal(x: Int, y: Int, z: Int): Boolean {
    return goalX == x && goalY == y && goalZ == z
  }

  companion object {
    private val SQRT_2 = sqrt(2.0)
  }

}
