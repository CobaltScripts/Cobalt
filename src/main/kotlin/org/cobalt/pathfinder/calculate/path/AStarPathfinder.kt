package org.cobalt.pathfinder.calculate.path

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import kotlin.time.Duration.Companion.nanoseconds
import org.cobalt.pathfinder.PathFindingConfig
import org.cobalt.pathfinder.calculate.Path
import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.calculate.openset.BinaryHeapOpenSet
import org.cobalt.pathfinder.goal.Goal
import org.cobalt.pathfinder.movement.CalculationContext
import org.cobalt.pathfinder.movement.Movement
import org.cobalt.pathfinder.movement.MovementResult
import org.cobalt.util.ChatUtils
import org.cobalt.util.MessageType
import org.cobalt.util.PlayerUtils

object AStarPathfinder {
  private val nodeCache = Long2ObjectOpenHashMap<PathNode>()
  private var goal: Goal? = null // This can't be null after findPath is called.
  private var availableMovements: Array<out Movement>? = null // This can't be null after findPath is called.

  fun findPath(goal: Goal, movements: Array<out Movement>, returnBestNode: Boolean): Path? {
    val startPos = PlayerUtils.position
    val calculationContext = CalculationContext()
    val openSet = BinaryHeapOpenSet()
    val movementResult = MovementResult()

    // After this, goal and movements should never be null again, so we can do !! in private functions safely.
    // If they are null, someone nuked the code, and we should get angry at them.
    AStarPathfinder.goal = goal
    availableMovements = movements

    val startNode = PathNode(startPos.x, startPos.y, startPos.z, goal)

    startNode.costSoFar = 0.0
    startNode.totalCost = startNode.costToEnd

    openSet.add(startNode)

    val startTime = System.nanoTime()
    var bestNode = startNode

    val deadline = startTime + PathFindingConfig.maxCalculationTime
    ChatUtils.sendSystemMessage(
      "Starting pathfinding from ${startNode.block}",
      MessageType.DEBUG
    )

    while (!openSet.isEmpty()
      && System.nanoTime() < deadline
    ) {
      val currentNode = openSet.poll()

      if (currentNode < bestNode) bestNode = currentNode

      if (goal.isAtGoal(currentNode.x, currentNode.y, currentNode.z)) {
        return reconstruct(currentNode, startTime)
      }

      evaluateMovements(currentNode, calculationContext, movementResult, openSet)
    }

    return if (returnBestNode) {
      reconstruct(bestNode, startTime)
    } else {
      null
    }
  }

  private fun evaluateMovements(
    currentNode: PathNode,
    calculationContext: CalculationContext,
    movementResult: MovementResult,
    openSet: BinaryHeapOpenSet,
  ) {
    for (move in availableMovements!!) {
      movementResult.reset()
      move.calculateCost(calculationContext, currentNode, movementResult)

      if (movementResult.cost < calculationContext.infCost) {
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
    val neighborCostSoFar = currentNode.costSoFar + movementResult.cost
    val neighborNode = getOrCacheNode(
      movementResult.x, movementResult.y, movementResult.z,
      PathNode.longHash(movementResult.x, movementResult.y, movementResult.z)
    )

    if (neighborCostSoFar >= neighborNode.costSoFar) return

    neighborNode.parent = currentNode
    neighborNode.costSoFar = neighborCostSoFar
    neighborNode.totalCost = neighborCostSoFar + neighborNode.costToEnd
    neighborNode.movementType = movement.type

    if (neighborNode.heapPosition == -1) {
      openSet.add(neighborNode)
    } else {
      openSet.relocate(neighborNode)
    }
  }

  fun getOrCacheNode(x: Int, y: Int, z: Int, hash: Long): PathNode {
    var node: PathNode? = nodeCache.get(hash)

    if (node == null) {
      node = PathNode(x, y, z, goal!!)
      nodeCache.put(hash, node)
    }

    return node
  }

  private fun reconstruct(endNode: PathNode, startTime: Long): Path {
    val path = mutableListOf<PathNode>()
    var node: PathNode? = endNode

    while (node != null) {
      path.addFirst(node)
      node = node.parent
    }

    return Path(
      nodes = path,
      timeElapsed = (System.nanoTime() - startTime).nanoseconds,
      goal = goal!!
    )
  }

}
