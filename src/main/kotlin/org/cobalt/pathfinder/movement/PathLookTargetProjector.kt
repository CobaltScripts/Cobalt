package org.cobalt.pathfinder.movement

import net.minecraft.world.phys.Vec3
import org.cobalt.pathfinder.calculate.PathNode

object PathLookTargetProjector {
  private const val SEARCH_BACKWARD_SEGMENTS = 1
  private const val SEARCH_FORWARD_SEGMENTS = 3
  private const val ZERO = 0.0
  private const val FULL_SEGMENT = 1.0

  private data class ClosestSegment(
    val index: Int,
    val progress: Double,
  )

  private data class SegmentProjection(
    val progress: Double,
    val distanceSquared: Double,
  )

  @JvmStatic
  fun getRotationTarget(
    playerEyePosition: Vec3,
    pathNodes: List<PathNode>,
    currentPathIndex: Int,
    lookAheadDistance: Double = 3.0,
  ): Vec3 {
    val closestSegment = findClosestSegment(
      playerEyePosition,
      pathNodes,
      currentPathIndex
    )

    val horizontalTarget = moveAlongPath(
      pathNodes,
      closestSegment,
      lookAheadDistance
    )

    return Vec3(
      horizontalTarget.x,
      playerEyePosition.y,
      horizontalTarget.z
    )
  }

  private fun findClosestSegment(
    playerEyePosition: Vec3,
    pathNodes: List<PathNode>,
    currentPathIndex: Int,
  ): ClosestSegment {
    val playerPosition = Vec3(
      playerEyePosition.x,
      ZERO,
      playerEyePosition.z
    )

    var closestSegment = ClosestSegment(
      currentPathIndex,
      ZERO
    )

    var closestDistanceSquared = Double.MAX_VALUE

    val startIndex = maxOf(
      ZERO.toInt(),
      currentPathIndex - SEARCH_BACKWARD_SEGMENTS
    )

    val endIndex = minOf(
      pathNodes.lastIndex,
      currentPathIndex + SEARCH_FORWARD_SEGMENTS
    )

    for (segmentIndex in startIndex until endIndex) {
      val projection = findClosestPointOnSegment(
        playerPosition,
        pathNodes[segmentIndex].centerVec,
        pathNodes[segmentIndex + 1].centerVec
      )

      if (projection == null) {
        continue
      }

      if (projection.distanceSquared < closestDistanceSquared) {
        closestDistanceSquared = projection.distanceSquared
        closestSegment = ClosestSegment(
          segmentIndex,
          projection.progress
        )
      }
    }

    return closestSegment
  }

  private fun findClosestPointOnSegment(
    point: Vec3,
    start: Vec3,
    end: Vec3,
  ): SegmentProjection? {
    val direction = Vec3(
      end.x - start.x,
      ZERO,
      end.z - start.z
    )

    val lengthSquared =
      direction.x * direction.x +
        direction.z * direction.z

    if (lengthSquared == ZERO) {
      return null
    }

    val progress =
      (
        (point.x - start.x) * direction.x +
          (point.z - start.z) * direction.z
        )
        .coerceIn(ZERO, lengthSquared) / lengthSquared

    val closestX = start.x + direction.x * progress
    val closestZ = start.z + direction.z * progress

    val distanceX = point.x - closestX
    val distanceZ = point.z - closestZ

    return SegmentProjection(
      progress,
      distanceX * distanceX + distanceZ * distanceZ
    )
  }

  private fun moveAlongPath(
    pathNodes: List<PathNode>,
    startingSegment: ClosestSegment,
    distance: Double,
  ): Vec3 {
    var remainingDistance = distance

    val currentSegmentResult = moveOnSegment(
      pathNodes,
      startingSegment.index,
      startingSegment.progress,
      remainingDistance
    )

    if (currentSegmentResult != null) {
      return currentSegmentResult
    }

    remainingDistance -= distanceRemainingOnSegment(
      pathNodes,
      startingSegment
    )

    for (segmentIndex in startingSegment.index + 1 until pathNodes.lastIndex) {
      val result = moveOnSegment(
        pathNodes,
        segmentIndex,
        ZERO,
        remainingDistance
      )

      if (result != null) {
        return result
      }

      remainingDistance -= segmentLength(
        pathNodes,
        segmentIndex
      )
    }

    return extrapolatePastEnd(
      pathNodes,
      remainingDistance
    )
  }

  private fun moveOnSegment(
    pathNodes: List<PathNode>,
    segmentIndex: Int,
    startProgress: Double,
    distance: Double,
  ): Vec3? {
    val start = pathNodes[segmentIndex].centerVec
    val end = pathNodes[segmentIndex + 1].centerVec

    val direction = Vec3(
      end.x - start.x,
      ZERO,
      end.z - start.z
    )

    val length = direction.length()

    if (length == ZERO) {
      return null
    }

    val availableDistance =
      (FULL_SEGMENT - startProgress) * length

    if (distance > availableDistance) {
      return null
    }

    val progress =
      startProgress + distance / length

    return start.add(
      direction.scale(progress)
    )
  }

  private fun distanceRemainingOnSegment(
    pathNodes: List<PathNode>,
    segment: ClosestSegment,
  ): Double {
    return (FULL_SEGMENT - segment.progress) *
      segmentLength(pathNodes, segment.index)
  }

  private fun segmentLength(
    pathNodes: List<PathNode>,
    segmentIndex: Int,
  ): Double {
    val start = pathNodes[segmentIndex].centerVec
    val end = pathNodes[segmentIndex + 1].centerVec

    return Vec3(
      end.x - start.x,
      ZERO,
      end.z - start.z
    ).length()
  }

  private fun extrapolatePastEnd(
    pathNodes: List<PathNode>,
    distance: Double,
  ): Vec3 {
    val last = pathNodes.last()

    val previous =
      if (pathNodes.size > 1) {
        pathNodes[pathNodes.lastIndex - 1]
      } else {
        last
      }

    val direction = Vec3(
      (last.x - previous.x).toDouble(),
      ZERO,
      (last.z - previous.z).toDouble()
    )

    val length = direction.length()

    if (length == ZERO) {
      return last.centerVec
    }

    return last.centerVec.add(
      direction.scale(distance / length)
    )
  }
}
