package org.cobalt.pathfinder.cost

import kotlin.math.pow
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.chat.MessageType
import org.cobalt.pathfinder.movement.CalculationContext

class ActionCosts(private val ctx: CalculationContext, speedAmplifier: Int, jumpAmplifier: Int) {

  val infCost = 1e6

  val blockFallCost: DoubleArray = generateBlockFallCost()
  val jumpOneBlockCost: Double
  val yawTurnCost: Double

  val oneBlockWalkCost = 1.0 / speedEffect(speedAmplifier)
  val walkOffOneBlockCost = oneBlockWalkCost * 0.8
  val centerAfterFallCost = oneBlockWalkCost * 0.2

  init {
    ChatUtils.sendSystemMessage(
      "Speed Amplifier: $speedAmplifier, Jump Amplifier: $jumpAmplifier",
      MessageType.DEBUG
    )

    var vel = jumpVelocity(jumpAmplifier)
    var height = 0.0
    var time = 1.0
    var step = 0

    while (step < 20) {
      height += vel
      vel = (vel - 0.08) * 0.98

      if (vel < 0) {
        break
      }

      time++
      step++
    }

    jumpOneBlockCost = time + fallDistanceToTicks(height - 1)
    yawTurnCost = oneBlockWalkCost * 0.5
  }

  private fun speedEffect(amplifier: Int): Double {
    return if (amplifier < 0) 1.0 else 1.0 + (amplifier + 1) * 0.5
  }

  private fun jumpVelocity(amplifier: Int): Double {
    return 0.42 + (amplifier + 1) * 0.1
  }

  private fun fallDistanceToTicks(distance: Double): Double {
    if (distance == 0.0) return 0.0
    var tmpDistance = distance
    var tickCount = 0

    while (true) {
      val fallDistance = downwardMotionAtTick(tickCount)

      if (tmpDistance <= fallDistance) {
        return tickCount + tmpDistance / fallDistance
      }

      tmpDistance -= fallDistance
      tickCount++
    }
  }

  private fun downwardMotionAtTick(tick: Int): Double {
    return (0.98.pow(tick.toDouble()) - 1) * -3.92
  }

  private fun generateBlockFallCost(): DoubleArray {
    val timeCost = DoubleArray(ctx.maxFallDistance + 1)
    var currentDistance = 0.0
    var targetDistance = 1
    var tickCount = 0

    while (true) {
      val velocityAtTick = downwardMotionAtTick(tickCount)

      if (currentDistance + velocityAtTick >= targetDistance) {
        timeCost[targetDistance] = tickCount + (targetDistance - currentDistance) / velocityAtTick
        targetDistance++

        if (targetDistance > ctx.maxFallDistance) {
          break
        }

        continue
      }

      currentDistance += velocityAtTick
      tickCount++
    }

    return timeCost
  }

}
