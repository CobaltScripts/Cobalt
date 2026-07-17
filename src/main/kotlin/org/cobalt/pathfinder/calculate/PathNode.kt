package org.cobalt.pathfinder.calculate

import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import org.cobalt.pathfinder.goal.Goal
import org.cobalt.pathfinder.movement.Movement
import org.cobalt.pathfinder.movement.MovementType

data class PathNode(
  val x: Int,
  val y: Int,
  val z: Int,
  val goal: Goal,
) : Comparable<PathNode> {

  var costSoFar = 1e6
  val costToEnd = goal.heuristic(x, y, z)
  var totalCost = 1.0
  var heapPosition = -1

  var movement: Movement? = null
  var parent: PathNode? = null

  val block: BlockPos = BlockPos(x, y, z)

  val useMovementFly: Boolean
    get() = movement?.type == MovementType.FLY

  override fun compareTo(other: PathNode): Int {
    return compareValuesBy(
      this,
      other,
      PathNode::costToEnd,
      PathNode::costSoFar,
    )
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) {
      return true
    }

    if (other !is PathNode) {
      return false
    }

    return other.x == x &&
      other.y == y &&
      other.z == z
  }

  override fun hashCode(): Int {
    val hash = longHash(x, y, z)
    return (hash xor (hash ushr 32)).toInt()
  }

  companion object {
    fun longHash(x: Int, y: Int, z: Int): Long {
      var hash = 3241L
      hash = 3457689L * hash + x
      hash = 8734625L * hash + y
      hash = 2873465L * hash + z
      return hash
    }
  }

}
