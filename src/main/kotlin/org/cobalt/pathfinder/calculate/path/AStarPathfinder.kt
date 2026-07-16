package org.cobalt.pathfinder.calculate.path

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.math.abs
import kotlin.math.atan2
import org.cobalt.pathfinder.calculate.Path
import org.cobalt.pathfinder.calculate.PathMode
import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.calculate.openset.BinaryHeapOpenSet
import org.cobalt.pathfinder.goal.Goal
import org.cobalt.pathfinder.movement.CalculationContext
import org.cobalt.pathfinder.movement.Movement
import org.cobalt.pathfinder.movement.MovementResult
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.chat.MessageType
import org.cobalt.util.rotation.RotationMath

class AStarPathfinder(
  val startX: Int,
  val startY: Int,
  val startZ: Int,
  val goal: Goal,
  val mode: PathMode,
  val calculationContext: CalculationContext,
  val returnBestNode: Boolean,
  val maxCalculationTime: Long,
) {

  private val closedSet = Long2ObjectOpenHashMap<PathNode>()
  private val movements = mode.movements
  private var startTime = 0L

  fun findPath(): Path? {
    val openSet = BinaryHeapOpenSet()
    val movementResult = MovementResult()

    val startNode = PathNode(
      startX, startY, startZ, goal
    ).also {
      it.costSoFar = 0.0
      it.totalCost = it.costToEnd
    }

    openSet.add(startNode)
    startTime = System.nanoTime()

    var bestNode = startNode
    val deadline = startTime + maxCalculationTime

    ChatUtils.sendSystemMessage(
      "Starting pathfinding from ${startNode.block}",
      MessageType.DEBUG
    )

    while (!openSet.isEmpty() && System.nanoTime() < deadline) {
      val currentNode = openSet.poll()

      if (currentNode < bestNode) {
        bestNode = currentNode
      }

      if (goal.isAtGoal(currentNode.x, currentNode.y, currentNode.z)) {
        return reconstruct(currentNode)
      }

      evaluateMovements(
        calculationContext,
        currentNode,
        movementResult,
        openSet
      )
    }

    return if (returnBestNode) {
      reconstruct(bestNode)
    } else {
      null
    }
  }

  private fun evaluateMovements(
    calculationContext: CalculationContext,
    currentNode: PathNode,
    movementResult: MovementResult,
    openSet: BinaryHeapOpenSet,
  ) {
    for (move in movements) {
      movementResult.reset()
      move.calculateCost(calculationContext, currentNode, movementResult)

      if (movementResult.cost < calculationContext.costs.infCost) {
        relaxNeighbor(currentNode, move, movementResult, openSet)
      }
    }
  }

  private fun relaxNeighbor(
    currentNode: PathNode,
    movement: Movement,
    movementResult: MovementResult,
    openSet: BinaryHeapOpenSet,
  ) {
    val turnCost = yawTurnCost(currentNode, movementResult)
    val neighborCostSoFar = currentNode.costSoFar + movementResult.cost + turnCost
    val neighborNode = getNode(
      movementResult.x, movementResult.y, movementResult.z,
      PathNode.longHash(movementResult.x, movementResult.y, movementResult.z)
    )

    if (neighborCostSoFar >= neighborNode.costSoFar) {
      return
    }

    neighborNode.parent = currentNode
    neighborNode.costSoFar = neighborCostSoFar
    neighborNode.totalCost = neighborCostSoFar + neighborNode.costToEnd
    neighborNode.movement = movement

    if (neighborNode.heapPosition == -1) {
      openSet.add(neighborNode)
    } else {
      openSet.relocate(neighborNode)
    }
  }

  private fun yawTurnCost(currentNode: PathNode, movementResult: MovementResult): Double {
    val parent = currentNode.parent ?: return 0.0

    val prevDx = currentNode.x - parent.x
    val prevDz = currentNode.z - parent.z
    val nextDx = movementResult.x - currentNode.x
    val nextDz = movementResult.z - currentNode.z

    val prevYaw = yawOf(prevDx, prevDz) ?: return 0.0
    val nextYaw = yawOf(nextDx, nextDz) ?: return 0.0

    return if (abs(RotationMath.angleDifference(nextYaw, prevYaw)) > 35f) {
      calculationContext.costs.yawTurnCost
    } else {
      0.0
    }
  }

  private fun yawOf(dx: Int, dz: Int): Float? {
    if (dx == 0 && dz == 0) {
      return null
    }

    return (atan2(dz.toDouble(), dx.toDouble()) * RotationMath.RAD_TO_DEG - 90.0).toFloat()
  }

  fun getNode(x: Int, y: Int, z: Int, hash: Long): PathNode {
    var node: PathNode? = closedSet.get(hash)

    if (node == null) {
      node = PathNode(x, y, z, goal)
      closedSet.put(hash, node)
    }

    return node
  }

  private fun reconstruct(endNode: PathNode): Path {
    val path = mutableListOf<PathNode>()
    var node: PathNode? = endNode

    while (node != null) {
      path.addFirst(node)
      node = node.parent
    }

    return Path(
      nodes = path,
      timeElapsed = (System.nanoTime() - startTime).nanoseconds,
      goal = goal
    )
  }

}
