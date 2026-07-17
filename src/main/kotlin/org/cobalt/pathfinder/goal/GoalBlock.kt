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
    val dx = abs(x - goalX).toDouble()
    val dz = abs(z - goalZ).toDouble()

    val diagonal = min(dx, dz)
    val straight = max(dx, dz) - diagonal

    val horizontal =
      diagonal * (ctx.costs.oneBlockWalkCost * SQRT_2) +
        straight * ctx.costs.oneBlockWalkCost

    val dy = goalY - y

    val vertical = when {
      dy > 0 -> dy * ctx.costs.jumpOneBlockCost
      dy < 0 -> -dy * (ctx.costs.fallNBlocksCost[1] * 0.5)
      else -> 0.0
    }

    return horizontal + vertical
  }

  override fun isAtGoal(x: Int, y: Int, z: Int): Boolean {
    return goalX == x && goalY == y && goalZ == z
  }

  companion object {
    private val SQRT_2 = sqrt(2.0)
  }
  
}
