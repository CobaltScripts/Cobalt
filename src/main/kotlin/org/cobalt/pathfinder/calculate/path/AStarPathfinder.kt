package org.cobalt.pathfinder.calculate.path

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import kotlin.time.Duration.Companion.milliseconds
import org.cobalt.pathfinder.PathFindingConfig
import org.cobalt.pathfinder.calculate.Path
import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.calculate.openset.BinaryHeapOpenSet
import org.cobalt.pathfinder.goal.Goal
import org.cobalt.pathfinder.movement.CalculationContext
import org.cobalt.pathfinder.movement.Movement
import org.cobalt.pathfinder.movement.MovementResult

class AStarPathfinder(
  val startX: Int,
  val startY: Int,
  val startZ: Int,
  val goal: Goal,
  val movements: Array<out Movement>,
  val returnBestNode: Boolean,
) {

  private val nodeCache = Long2ObjectOpenHashMap<PathNode>()

  fun findPath(): Path? {
    val calculationContext = CalculationContext()
    val openSet = BinaryHeapOpenSet()
    val movementResult = MovementResult()

    val startNode = PathNode(
      startX, startY, startZ, goal
    )

    startNode.costSoFar = 0.0
    startNode.totalCost = startNode.costToEnd

    openSet.add(startNode)

    val startTime = System.nanoTime()
    var bestNode = startNode

    val deadline = startTime + PathFindingConfig.maxCalculationTime

    while (!openSet.isEmpty() && System.nanoTime() < deadline) {
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
    for (move in movements) {
      movementResult.reset()
      move.calculateCost(calculationContext, currentNode, movementResult)

      if (movementResult.cost < calculationContext.infCost) relaxNeighbor(currentNode, move, movementResult, openSet)
    }
  }

  private fun relaxNeighbor(
    currentNode: PathNode,
    movement: Movement,
    movementResult: MovementResult,
    openSet: BinaryHeapOpenSet
  ) {
    val neighborCostSoFar = currentNode.costSoFar + movementResult.cost
    val neighborNode = getNode(
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

  fun getNode(x: Int, y: Int, z: Int, hash: Long): PathNode {
    var node: PathNode? = nodeCache.get(hash)

    if (node == null) {
      node = PathNode(x, y, z, goal)
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
      timeElapsed = (System.nanoTime() - startTime).milliseconds,
      goal = goal
    )
  }

}
