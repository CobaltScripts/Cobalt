package org.cobalt.pathfinder.state.pathing

import org.cobalt.pathfinder.PathExecutor
import org.cobalt.pathfinder.calculate.Path
import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.state.ExecutorState
import org.cobalt.pathfinder.state.fly.StartFlyState
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.chat.MessageType
import org.cobalt.util.client.PlayerUtils

class PathingState : ExecutorState() {

  private var path: Path? = null

  private inline var currPathIndex
    get() = PathExecutor.pathIndex
    set(value) {
      PathExecutor.pathIndex = value
    }

  override fun enter() {
    path = PathExecutor.path ?: run {
      ChatUtils.sendSystemMessage("<red>Cannot traverse a nonexistent path...</red>")
      PathExecutor.stop()
      return
    }
  }

  override fun exit() {
    playerInput.stopMovement()
  }

  override fun onTick() {
    val path = path ?: return

    val nodes = path.nodes
    val targetNode = nodes[currPathIndex]

    if (targetNode.useMovementFly && PlayerUtils.canFly && !PlayerUtils.isFlying) {
      PathExecutor.changeState(StartFlyState())
    }

    if (hasArrived(targetNode)) {
      currPathIndex++

      if (currPathIndex >= nodes.size) {
        ChatUtils.sendSystemMessage("Completed Path!", MessageType.DEBUG)
        PathExecutor.stop()
      }

      return
    }

    // TODO: ROTATIONS & MOVEMENT
  }

  private fun hasArrived(node: PathNode): Boolean {
    return node.block == PlayerUtils.position
  }

}
