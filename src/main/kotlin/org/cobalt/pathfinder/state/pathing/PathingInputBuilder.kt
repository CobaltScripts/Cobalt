package org.cobalt.pathfinder.state.pathing

import net.minecraft.core.BlockPos
import org.cobalt.pathfinder.PathFindingConfig
import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.helper.PlayerInput
import org.cobalt.pathfinder.movement.MovementInputMapper
import org.cobalt.util.PlayerUtils
import org.cobalt.util.RotationUtils


class PathingInputBuilder(private val jumpDecision: JumpDecision) {
  fun build(
    playerPos: BlockPos,
    node: PathNode,
    nodes: List<PathNode>,
    sameXZ: Boolean,
    currentIndex: Int,
  ): PlayerInput {
    return if (node.isFly && sameXZ) {
      buildFlyVerticalInput(node, playerPos)
    } else {
      buildStandardInput(playerPos, node, nodes, sameXZ, currentIndex)
    }
  }

  private fun buildFlyVerticalInput(
    node: PathNode,
    playerPos: BlockPos,
  ): PlayerInput {
    val input = PlayerInput()

    val diffY = node.block.y - playerPos.y
    when {
      diffY > 0 -> input.jump = true
      diffY < 0 -> input.sneak = true
    }

    return input
  }

  private fun buildStandardInput(
    playerPos: BlockPos,
    node: PathNode,
    nodes: List<PathNode>,
    sameXZ: Boolean,
    currentIndex: Int,
  ): PlayerInput {
    val input = PlayerInput()

    applyHorizontalInput(input, node, sameXZ)
    applySprintIfNeeded(input)
    applyJumpIfNeeded(input, playerPos, nodes, currentIndex, node)

    return input
  }

  private fun applyHorizontalInput(
    input: PlayerInput,
    node: PathNode,
    sameXZ: Boolean,
  ) {
    if (sameXZ) {
      return
    }

    val neededKeys = MovementInputMapper.getNeededKeys(
      PlayerUtils.rotation.yaw,
      RotationUtils.getRotation(node.centerVec).yaw
    )

    input.apply(neededKeys)
  }

  private fun applySprintIfNeeded(input: PlayerInput) {
    if (PathFindingConfig.shouldSprint) {
      input.sprint = true
    }
  }

  private fun applyJumpIfNeeded(
    input: PlayerInput,
    playerPos: BlockPos,
    nodes: List<PathNode>,
    currentIndex: Int,
    node: PathNode,
  ) {
    if (node.isFly) {
      return
    }

    if (jumpDecision.shouldJump(playerPos, nodes, currentIndex)) {
      input.jump = true
    }
  }
}
