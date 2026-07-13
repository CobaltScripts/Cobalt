package org.cobalt.pathfinder.state.impl

import java.awt.Color
import kotlin.math.abs
import kotlin.random.Random
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import org.cobalt.Cobalt.minecraft
import org.cobalt.dsl.centerVec
import org.cobalt.module.impl.misc.Debug
import org.cobalt.module.impl.misc.Rotations
import org.cobalt.pathfinder.PathFindingConfig
import org.cobalt.pathfinder.PathFindingFacade
import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.helper.PathRenderer
import org.cobalt.pathfinder.helper.PlayerInput
import org.cobalt.pathfinder.movement.MovementHelper
import org.cobalt.pathfinder.state.ExecutorState
import org.cobalt.util.PlayerUtils
import org.cobalt.util.RotationUtils
import org.cobalt.util.WorldRenderUtils
import org.cobalt.util.helper.Clock
import org.cobalt.util.rotation.RotationTarget

class PathingState : ExecutorState() {

  private val path = PathFindingFacade.path
  private val movements = PathFindingFacade.availableMovements
  private val jumpDelay = Clock()

  private inline val pathIndex: Int
    get() = PathFindingFacade.pathIndex

  override fun exit() {
    input.stopMovement()
    Rotations.stop()
  }

  override fun onTick() {
    if (path == null || movements == null) {
      PathFindingFacade.stop()
      return
    }

    val player = PlayerUtils.player ?: return
    val nodes = path.nodes
    val node = nodes[pathIndex]
    val playerPos = PlayerUtils.position

    if (
      advanceReachedNodes(playerPos, nodes) ||
      shouldStartFlying(node)
    ) {
      return
    }

    val sameXZ = node.block.x == playerPos.x && node.block.z == playerPos.z
    val movement = buildPlayerInput(playerPos, node, nodes, sameXZ)
    val lookTarget = MovementHelper.getRotationTarget(
      player.eyePosition, nodes, pathIndex
    )

    Rotations.track(RotationTarget(lookTarget))
    input.applyInput(movement)
  }

  override fun onRender() {
    if (path == null || movements == null) {
      PathFindingFacade.stop()
      return
    }

    PathRenderer.render()

    if (!Debug.enabled) {
      return
    }

    val lookAhead = (PlayerUtils.velocity.length() * 6.0).coerceIn(5.0, 32.0)
    val target = MovementHelper.getRotationTarget(
      PlayerUtils.player!!.eyePosition, path.nodes, pathIndex, lookAhead
    )

    WorldRenderUtils.drawBox(
      AABB(
        target.x - 0.25, target.y - 0.25, target.z - 0.25,
        target.x + 0.25, target.y + 0.25, target.z + 0.25
      ), Color.GREEN
    )
  }

  private fun advanceReachedNodes(playerPos: BlockPos, nodes: List<PathNode>): Boolean {
    if (!hasReached(playerPos, nodes, pathIndex)) {
      return false
    }

    while (
      pathIndex + 1 < nodes.size &&
      hasReached(playerPos, nodes, pathIndex)
    ) {
      PathFindingFacade.pathIndex++
    }

    if (pathIndex + 1 >= nodes.size) {
      PathFindingFacade.stop()
    }

    return true
  }

  private fun shouldStartFlying(node: PathNode): Boolean {
    requireNotNull(movements)

    if (
      !node.isFly ||
      PlayerUtils.isFlying
    ) {
      return false
    }

    PathFindingFacade.changeState(StartFlyState())
    return true
  }

  private fun buildPlayerInput(
    playerPos: BlockPos,
    node: PathNode,
    nodes: List<PathNode>,
    sameXZ: Boolean,
  ): PlayerInput {
    val index = pathIndex
    val input = PlayerInput()

    if (node.isFly && sameXZ) {
      val diffY = node.block.y - playerPos.y
      when {
        diffY > 0 -> input.jump = true
        diffY < 0 -> input.sneak = true
      }

      if (input.jump || input.sneak) return input
    }

    if (!sameXZ) {
      val neededKeys = MovementHelper.getNeededKeys(
        PlayerUtils.rotation.yaw,
        RotationUtils.getRotation(node.centerVec).yaw
      )

      input.apply(neededKeys)
    }

    if (PathFindingConfig.shouldSprint) input.sprint = true

    if (!node.isFly) {
      if (shouldJump(playerPos, nodes, index)) input.jump = true
    }

    if (node.isFly && sameXZ) {
      val diffY = node.block.y - playerPos.y
      when {
        diffY > 0 -> input.jump = true
        diffY < 0 -> input.sneak = true
      }
    }

    return input
  }

  private fun hasReached(
    playerPos: BlockPos,
    nodes: List<PathNode>,
    currentIndex: Int,
  ): Boolean {
    val node = nodes[currentIndex]

    val nodeCenter = node.centerVec
    val playerVec = playerPos.centerVec()

    if (playerVec.distanceToSqr(nodeCenter) < PathFindingConfig.hasReachedThreshold) return true

    if (currentIndex + 1 >= nodes.size) return false

    val level = minecraft.level
    requireNotNull(level)

    if (
      !MovementHelper.isBottomSlab(level.getBlockState(node.blockStandingOn)) &&
      (node.block.y > playerPos.y || !PlayerUtils.onGround)
    ) {
      return false
    }

    val segment = nodes[currentIndex + 1].centerVec.subtract(nodeCenter)
    val toPlayer = playerVec.subtract(nodeCenter)

    if (toPlayer.dot(segment) < 0.0) {
      return false
    }

    val squaredPerpendicularDistance = toPlayer.cross(segment).lengthSqr() / segment.lengthSqr()
    return squaredPerpendicularDistance < 1.0
  }

  private fun shouldJump(
    playerPos: BlockPos,
    nodes: List<PathNode>,
    currentIndex: Int,
  ): Boolean {
    val level = minecraft.level
    requireNotNull(level)

    if (!PlayerUtils.onGround || !jumpDelay.passed()) {
      return false
    }

    val node = nodes[currentIndex]

    if (node.block.y - playerPos.y < 1 ||
      MovementHelper.isBottomSlab(level.getBlockState(node.blockStandingOn))
    ) {
      return false
    }

    val nodeCenter = node.centerVec
    val playerVec = playerPos.centerVec()
    val dx = abs(nodeCenter.x - playerVec.x)
    val dz = abs(nodeCenter.z - playerVec.z)

    if (dx + dz > 1.2 || minOf(dx, dz) > 0.2) {
      return false
    }

    if (PlayerUtils.canFly) {
      jumpDelay.schedule(Random.nextLong(350, 450))
    }

    return true
  }

}
