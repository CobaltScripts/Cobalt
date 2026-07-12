package org.cobalt.pathfinder.state.impl

import java.awt.Color
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.phys.AABB
import org.cobalt.Cobalt.minecraft
import org.cobalt.dsl.centerVec
import org.cobalt.module.impl.misc.Debug
import org.cobalt.module.impl.misc.Rotations
import org.cobalt.pathfinder.PathExecutor
import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.helper.PlayerInput
import org.cobalt.pathfinder.movement.MovementHelper
import org.cobalt.pathfinder.precompute.Ternary
import org.cobalt.pathfinder.state.ExecutorState
import org.cobalt.util.PlayerUtils
import org.cobalt.util.RotationUtils
import org.cobalt.util.WorldRenderUtils
import org.cobalt.util.helper.Clock
import org.cobalt.util.rotation.RotationTarget

class PathingState : ExecutorState() {

  private val path = PathExecutor.path
  private val config = PathExecutor.config
  private val jumpDelay = Clock()

  override fun exit() {
    input.stopMovement()
    Rotations.stop()
  }

  override fun onTick() {
    if (path == null || config == null) {
      PathExecutor.stop()
      return
    }

    val player = PlayerUtils.player ?: return
    val index = PathExecutor.pathIndex
    val node = path.nodes[index]

    val playerPos = PlayerUtils.position
    val nodePos = node.block

    if (hasReached(playerPos, path.nodes, index)) {
      if (index + 1 >= path.nodes.size) {
        PathExecutor.stop()
        return
      }

      PathExecutor.pathIndex++
      return
    }

    if (
      node.isFly &&
      config.useFlyMovement &&
      PlayerUtils.canFly &&
      !PlayerUtils.isFlying
    ) {
      PathExecutor.changeState(StartFlyState())
      return
    }

    val sameXZ = nodePos.x == playerPos.x && nodePos.z == playerPos.z
    val lookAhead = (PlayerUtils.velocity.length() * 2.0).coerceIn(5.0, 20.0)
    val targetVec = MovementHelper.getRotationTarget(player.eyePosition, path.nodes, index, lookAhead)

    val playerInput = PlayerInput()
    val neededKeys = MovementHelper.getNeededKeys(
      PlayerUtils.rotation.yaw,
      RotationUtils.getRotation(targetVec).yaw
    )

    if (!node.isFly || !sameXZ) {
      playerInput.apply(neededKeys)
    }

    if (!node.isFly) {
      if (config.shouldSprint) {
        playerInput.sprint = true
      }

      if (shouldJump(playerPos, path.nodes, index)) {
        playerInput.jump = true
      }
    }

    if (node.isFly && sameXZ) {
      val diffY = nodePos.y - playerPos.y

      when {
        diffY > 0 -> playerInput.jump = true
        diffY < 0 -> playerInput.sneak = true
      }
    }

    Rotations.track(RotationTarget(targetVec))
    input.applyInput(playerInput)
  }

  override fun onRender() {
    if (path == null || config == null) {
      PathExecutor.stop()
      return
    }

    if (!Debug.enabled) {
      return
    }

    val index = PathExecutor.pathIndex
    val lookAhead = (PlayerUtils.velocity.length() * 1.2).coerceIn(5.0, 10.0)
    val targetVec = MovementHelper.getRotationTarget(PlayerUtils.player!!.eyePosition, path.nodes, index, lookAhead)

    WorldRenderUtils.drawBox(
      AABB(
        targetVec.x - 0.25,
        targetVec.y - 0.25,
        targetVec.z - 0.25,
        targetVec.x + 0.25,
        targetVec.y + 0.25,
        targetVec.z + 0.25
      ), Color.GREEN
    )
  }

  private fun hasReached(
    playerPos: BlockPos,
    nodes: List<PathNode>,
    currentIndex: Int,
  ): Boolean {
    val level = minecraft.level ?: return false

    val currNode = nodes[currentIndex]
    val currNodePos = currNode.centerVec
    val playerVec = playerPos.centerVec()

    if (currNode.isFly) {
      return false
    }

    if (playerVec.distanceToSqr(currNodePos) < 0.3 * 0.3) {
      return true
    }

    if (currentIndex + 1 >= nodes.size) {
      return false
    }

    val nextNode = nodes[currentIndex + 1]

    val isSlab = MovementHelper.isBottomSlab(
      level.getBlockState(currNode.blockStandingOn)
    )

    if (!isSlab && (currNode.block.y > playerPos.y || !PlayerUtils.onGround)) {
      return false
    }

    val nextNodePos = nextNode.centerVec
    val segment = nextNodePos.subtract(currNodePos)
    val toPlayer = playerVec.subtract(currNodePos)

    if (toPlayer.dot(segment) < 0.0) {
      return false
    }

    val cross = toPlayer.cross(segment)
    val perpDistSq = cross.lengthSqr() / segment.lengthSqr()

    return perpDistSq < 1.0
  }

  private fun shouldJump(
    playerPos: BlockPos,
    nodes: List<PathNode>,
    currentIndex: Int
  ): Boolean {
    val level = minecraft.level ?: return false

    if (!PlayerUtils.onGround || !jumpDelay.passed()) {
      return false
    }

    val currNode = nodes[currentIndex]
    if (currNode.block.y - playerPos.y < 1) {
      return false
    }

    if (MovementHelper.isBottomSlab(
        level.getBlockState(currNode.blockStandingOn)
      )) {
      return false
    }

    val currNodeCenter = currNode.centerVec
    val playerVec = playerPos.centerVec()
    val dx = abs(currNodeCenter.x - playerVec.x)
    val dz = abs(currNodeCenter.z - playerVec.z)
    val flatDist = dx + dz

    if (flatDist > 1.2) {
      return false
    }

    val sideDist = if (dx > dz) dz else dx

    if (sideDist > 0.2) {
      return false
    }

    if (PlayerUtils.canFly) {
      jumpDelay.schedule(370)
    }

    return true
  }

}
