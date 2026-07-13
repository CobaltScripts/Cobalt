package org.cobalt.pathfinder.state.pathing

import net.minecraft.world.phys.AABB
import org.cobalt.module.impl.misc.Debug
import org.cobalt.module.impl.misc.Rotations
import org.cobalt.pathfinder.PathFindingFacade
import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.helper.PathRenderer
import org.cobalt.pathfinder.movement.PathLookTargetProjector
import org.cobalt.pathfinder.state.ExecutorState
import org.cobalt.pathfinder.state.fly.StartFlyState
import org.cobalt.util.PlayerUtils
import org.cobalt.util.WorldRenderUtils
import org.cobalt.util.rotation.RotationTarget
import java.awt.Color

class PathingState : ExecutorState() {
  private val path = PathFindingFacade.path
  private val movements = PathFindingFacade.availableMovements
  private val jumpDecision = JumpDecision()
  private val inputBuilder = PathingInputBuilder(jumpDecision)
  private val advancer = PathingAdvancer()

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
        advancer.advanceIfReached(playerPos, nodes) ||
      shouldStartFlying(node)
    ) {
      return
    }

    val sameXZ = node.block.x == playerPos.x && node.block.z == playerPos.z
    val movement = inputBuilder.build(playerPos, node, nodes, sameXZ, pathIndex)
    val lookTarget = PathLookTargetProjector.getRotationTarget(
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
    val target = PathLookTargetProjector.getRotationTarget(
      PlayerUtils.player!!.eyePosition, path.nodes, pathIndex, lookAhead
    )

    WorldRenderUtils.drawBox(
        AABB(
            target.x - 0.25, target.y - 0.25, target.z - 0.25,
            target.x + 0.25, target.y + 0.25, target.z + 0.25
        ), Color.GREEN
    )
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

}
