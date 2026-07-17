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
    var heuristic = 0.0

    val xDiff = x - goalX
    val yDiff = y - goalY
    val zDiff = z - goalZ

    if (yDiff > 0) {
      heuristic += (ctx.costs.fallNBlocksCost[2] / 2.0) * yDiff
    } else if (yDiff < 0) {
      heuristic += -yDiff * ctx.costs.jumpOneBlockCost
    }

    val absX = abs(xDiff.toDouble())
    val absZ = abs(zDiff.toDouble())
    val diagonal = min(absX, absZ)
    val straight = max(absX, absZ) - diagonal

    heuristic += (diagonal * SQRT_2 + straight) * 3.563
    return heuristic
  }

  override fun isAtGoal(x: Int, y: Int, z: Int): Boolean {
    return goalX == x && goalY == y && goalZ == z
  }

  companion object {
    private val SQRT_2 = sqrt(2.0)
  }
}
