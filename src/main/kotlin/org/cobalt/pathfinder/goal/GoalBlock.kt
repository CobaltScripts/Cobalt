package org.cobalt.pathfinder.goal

import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt
import org.cobalt.pathfinder.movement.CalculationContext

class GoalBlock(
  val goalX: Int,
  val goalY: Int,
  val goalZ: Int,
  val ctx: CalculationContext
) : Goal {

  private val sqrt2 = sqrt(2.0)

  override fun heuristic(x: Int, y: Int, z: Int): Double {
    val dx = abs(goalX - x)
    val dz = abs(goalZ - z)
    val straight = abs(dx - dz).toDouble()
    var vertical = abs(goalY - y).toDouble()
    val diagonal = min(dx, dz).toDouble()

    if (goalY > y) {
      vertical *= 6.234399666206506
    } else {
      vertical *= ctx.costs.blockFallCost[2] / 2.0
    }

    return (straight + diagonal * sqrt2) * ctx.costs.oneBlockWalkCost + vertical
  }

  override fun isAtGoal(x: Int, y: Int, z: Int): Boolean {
    return goalX == x && goalY == y && goalZ == z
  }

}
