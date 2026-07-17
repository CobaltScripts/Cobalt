package org.cobalt.pathfinder.cost

import kotlin.math.pow
import org.cobalt.pathfinder.movement.CalculationContext

class ActionCosts(val ctx: CalculationContext) {

  val infCost = 1e4
  val oneBlockWalkCost = 1.0
  val fallNBlocksCost: DoubleArray = DoubleArray(4097) { i -> distanceToTicks(i.toDouble()) }
  val jumpOneBlockCost: Double

  init {
    val initialVelocity = 0.42 + (0.1 * ctx.jumpAmplifier)
    val jumpPeakHeight = calculateJumpPeakHeight(initialVelocity)
    val remainderHeight = jumpPeakHeight - 1.0
    val fallFromPeakCost = distanceToTicks(jumpPeakHeight)
    val fallRemainderCost = distanceToTicks(remainderHeight)
    this.jumpOneBlockCost = fallFromPeakCost - fallRemainderCost
  }

  private fun velocity(ticks: Int): Double {
    return (0.98.pow(ticks) - 1.0) * -3.92
  }

  fun distanceToTicks(distance: Double): Double {
    if (distance <= 0.0) {
      return 0.0
    }

    var tmpDistance = distance
    var tickCount = 0

    while (true) {
      val fallDistance = velocity(tickCount)

      if (tmpDistance <= fallDistance) {
        return tickCount + tmpDistance / fallDistance
      }

      tmpDistance -= fallDistance
      tickCount++
    }
  }

  private fun calculateJumpPeakHeight(initialVelocity: Double): Double {
    var vel = initialVelocity
    var totalHeight = 0.0

    while (vel > 0.0) {
      totalHeight += vel
      vel = (vel - 0.08) * 0.98
    }

    return totalHeight
  }

}
