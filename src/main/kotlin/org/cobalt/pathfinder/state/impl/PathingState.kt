package org.cobalt.pathfinder.state.impl

import org.cobalt.module.impl.misc.Rotations
import org.cobalt.pathfinder.PathConfig
import org.cobalt.pathfinder.PathExecutor
import org.cobalt.pathfinder.calculate.Path
import org.cobalt.pathfinder.movement.MovementStatus
import org.cobalt.pathfinder.state.ExecutorState
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.chat.MessageType
import org.cobalt.util.client.PlayerUtils
import org.cobalt.util.rotation.data.RotationTarget

class PathingState : ExecutorState() {

  private var path: Path? = null
  private var config: PathConfig? = null

  private inline var currPathIndex: Int
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
    movementController.stopMovement()
  }

  override fun onTick() {
    val path = path ?: return
    val config = config ?: return

    val nodes = path.nodes
    val targetNode = nodes[currPathIndex]

    val movement = targetNode.movement
    val state = movement?.updateState(config, nodes, currPathIndex) ?: return

    when (state.status) {
      MovementStatus.REACHED -> {
        currPathIndex++

        if (currPathIndex >= nodes.size) {
          ChatUtils.sendSystemMessage("Completed Path!", MessageType.DEBUG)
          PathExecutor.stop()
        }

        return
      }

      MovementStatus.UNREACHED -> {
        if (targetNode.useMovementFly && PlayerUtils.canFly && !PlayerUtils.isFlying) {
          PathExecutor.changeState(StartFlyState())
          return
        }

        state.target?.let { target ->
          movementController.applyInput(target.input)
          target.lookAt?.let(::RotationTarget)?.let(Rotations::track)
        }
      }

      MovementStatus.FAILED -> {
        PathExecutor.changeState(CalculatingState())
      }
    }
  }

}
