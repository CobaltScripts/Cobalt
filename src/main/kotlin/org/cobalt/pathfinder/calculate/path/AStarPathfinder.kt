package org.cobalt.pathfinder.calculate.path

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import kotlin.time.Duration.Companion.milliseconds
import org.cobalt.pathfinder.PathFinderConfig
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

  private val closedSet = Long2ObjectOpenHashMap<PathNode>()
  private var startTime = 0L

  fun findPath(): Path? {
    val ctx = CalculationContext()
    val openSet = BinaryHeapOpenSet()
    val res = MovementResult()

    val startNode = PathNode(
      startX, startY, startZ, goal
    ).also {
      it.costSoFar = 0.0
      it.totalCost = it.costToEnd
    }

    openSet.add(startNode)

    startTime = System.currentTimeMillis()
    var bestNode = startNode

    while (!openSet.isEmpty()) {
      if (System.currentTimeMillis() - startTime >= PathFinderConfig.maxCalculationTime) {
        break
      }

      val currentNode = openSet.poll()

      if (
        currentNode.costToEnd < bestNode.costToEnd ||
        (currentNode.costToEnd == bestNode.costToEnd &&
          currentNode.costSoFar < bestNode.costSoFar)
      ) {
        bestNode = currentNode
      }

      if (goal.isAtGoal(currentNode.x, currentNode.y, currentNode.z)) {
        return reconstruct(currentNode)
      }

      evaluateMovements(currentNode, ctx, res, openSet)
    }

    return if (returnBestNode) {
      reconstruct(bestNode)
    } else {
      null
    }
  }

  private fun evaluateMovements(
    currentNode: PathNode,
    ctx: CalculationContext,
    res: MovementResult,
    openSet: BinaryHeapOpenSet,
  ) {
    for (move in movements) {
      res.reset()
      move.calculateCost(ctx, currentNode, res)

      if (res.cost >= ctx.infCost) {
        continue
      }

      val neighborCostSoFar = currentNode.costSoFar + res.cost
      val neighborNode = getNode(
        res.x, res.y, res.z,
        PathNode.longHash(res.x, res.y, res.z)
      )

      if (neighborCostSoFar < neighborNode.costSoFar) {
        neighborNode.parent = currentNode
        neighborNode.costSoFar = neighborCostSoFar
        neighborNode.totalCost = neighborCostSoFar + neighborNode.costToEnd
        neighborNode.movementType = move.movementType

        if (neighborNode.heapPosition == -1) {
          openSet.add(neighborNode)
        } else {
          openSet.relocate(neighborNode)
        }
      }
    }
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
      timeElapsed = (System.currentTimeMillis() - startTime).milliseconds,
      goal = goal
    )
  }

}
