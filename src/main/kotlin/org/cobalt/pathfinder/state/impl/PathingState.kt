package org.cobalt.pathfinder.state.impl

import java.awt.Color
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import org.cobalt.dsl.centerVec
import org.cobalt.module.impl.misc.Rotations
import org.cobalt.pathfinder.PathExecutor
import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.helper.PlayerInput
import org.cobalt.pathfinder.movement.MovementHelper
import org.cobalt.pathfinder.state.ExecutorState
import org.cobalt.util.PlayerUtils
import org.cobalt.util.RotationUtils
import org.cobalt.util.WorldRenderUtils
import org.cobalt.util.helper.Clock
import org.cobalt.util.rotation.RotationTarget

class PathingState : ExecutorState() {

  private val path = PathExecutor.path
  private val config = PathExecutor.config

  override fun exit() {
    input.stopMovement()
    Rotations.stop()
  }

  override fun onTick() {
    if (path == null || config == null) {
      PathExecutor.stop()
      return
    }

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
    val targetVec = MovementHelper.getRotationTarget(
      playerPos.centerVec(), path.nodes, index
    )

    val playerInput = PlayerInput()
    val neededKeys = MovementHelper.getNeededKeys(
      PlayerUtils.rotation.yaw,
      RotationUtils.getRotation(node.topCenterVec).yaw
    )

    if (!sameXZ) {
      playerInput.apply(neededKeys)
    }

    if (!node.isFly) {
      if (config.shouldSprint) {
        playerInput.sprint = true
      }

      if (shouldJump(playerPos, path.nodes, index)) { // TODO: make it actually good
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

    val index = PathExecutor.pathIndex
    val playerPos = PlayerUtils.position

    val targetVec = MovementHelper.getRotationTarget(
      playerPos.centerVec(), path.nodes, index
    )

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

  // TODO: make it actually good
  private fun hasReached(
    playerPos: BlockPos,
    nodes: List<PathNode>,
    currentIndex: Int,
  ): Boolean {
    val currNode = nodes[currentIndex]
    val currNodePos = currNode.centerVec
    val playerVec = playerPos.centerVec()

    if (currNode.block.y - playerPos.y >= 1) {
      return currNode.block == playerPos
    }

    if (playerVec.distanceToSqr(currNodePos) < 0.3 * 0.3) {
      return true
    }

    // TODO: Fly pathfinder overshooting detection (its diff from walk)
    if (currNode.isFly) {
      return false
    }

    if (currentIndex + 1 >= nodes.size) {
      return false
    }

    val nextNodePos = nodes[currentIndex + 1].centerVec
    val segment = nextNodePos.subtract(currNodePos)
    val toPlayer = playerVec.subtract(currNodePos)

    return toPlayer.dot(segment) >= 0.0
  }

  private val jumpDelay = Clock()

  private fun shouldJump(
    playerPos: BlockPos,
    nodes: List<PathNode>,
    currentIndex: Int
  ): Boolean {
    if (!PlayerUtils.onGround || !jumpDelay.passed()) {
      return false
    }

    val currNode = nodes[currentIndex]
    val result = currNode.block.y - playerPos.y >= 1

    if (result && PlayerUtils.canFly) {
      jumpDelay.schedule(370)
    }

    return result
  }

}
