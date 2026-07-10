package org.cobalt.pathfinder.state.impl

import java.awt.Color
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.cobalt.dsl.centerVec
import org.cobalt.module.impl.misc.Rotations
import org.cobalt.pathfinder.PathExecutor
import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.helper.PlayerInput
import org.cobalt.pathfinder.movement.Movement
import org.cobalt.pathfinder.movement.MovementHelper
import org.cobalt.pathfinder.state.ExecutorState
import org.cobalt.util.PlayerUtils
import org.cobalt.util.RotationUtils
import org.cobalt.util.WorldRenderUtils
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
    val node = path.keyNodes[index]

    val playerPos = PlayerUtils.position
    val nodePos = node.block
    val isFlyNode = node.type == Movement.Type.FLY

    if (hasReached(playerPos.centerVec(), path.keyNodes, index)) {
      if (index + 1 >= path.keyNodes.size) {
        PathExecutor.stop()
        return
      }

      PathExecutor.pathIndex++
      return
    }

    if (
      isFlyNode &&
      config.useFlyMovement &&
      PlayerUtils.canFly &&
      !PlayerUtils.isFlying
    ) {
      PathExecutor.changeState(StartFlyState())
      return
    }

    val sameXZ = nodePos.x == playerPos.x && nodePos.z == playerPos.z
    val targetVec = MovementHelper.getRotationTarget(
      playerPos.centerVec(), path.keyNodes, index
    )

    val playerInput = PlayerInput()
    val neededKeys = MovementHelper.getNeededKeys(
      PlayerUtils.rotation.yaw,
      RotationUtils.getRotation(node.topCenterVec).yaw
    )

    if (!sameXZ) {
      playerInput.apply(neededKeys)
    }

    if (config.shouldSprint && !isFlyNode) {
      playerInput.sprint = true
    }

    // TODO: add jump logic for walking
    // playerInput.jump = !PlayerUtils.isFlying

    if (isFlyNode && sameXZ) {
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
      playerPos.centerVec(), path.keyNodes, index
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

  private fun hasReached(
    playerPos: Vec3,
    nodes: List<PathNode>,
    currentIndex: Int
  ): Boolean {
    val currentNodePos = nodes[currentIndex].centerVec

    if (playerPos.distanceToSqr(currentNodePos) < 0.3 * 0.3) {
      return true
    }

    if (currentIndex + 1 >= nodes.size) {
      return false
    }

    val nextNodePos = nodes[currentIndex + 1].centerVec
    val segment = nextNodePos.subtract(currentNodePos)
    val toPlayer = playerPos.subtract(currentNodePos)

    return toPlayer.dot(segment) >= 0.0
  }

}
