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

  private var config: PathConfig? = null
  private var path: Path? = null

  private inline var currPathIndex: Int
    get() = PathExecutor.pathIndex
    set(value) {
      PathExecutor.pathIndex = value
    }

  override fun enter() {
    config = PathExecutor.config
    path = PathExecutor.path ?: run {
      ChatUtils.sendSystemMessage("<red>Cannot traverse a nonexistent path...</red>")
      PathExecutor.stop()
      return
    }
  }

  override fun exit() {
    Rotations.stop()
    movementController.stopMovement()
  }

  override fun onTick() {
    val config = config ?: return
    val path = path ?: return

    val nodes = path.nodes
    val targetNode = nodes[currPathIndex]

    val movement = targetNode.movement
    val state = movement?.updateState(config, nodes, currPathIndex) ?: return

    when (state.status) {
      MovementStatus.REACHED -> {
        ChatUtils.sendSystemMessage("Reached Node [$currPathIndex]!", MessageType.DEBUG)
        currPathIndex++

        if (currPathIndex >= nodes.size) {
          ChatUtils.sendSystemMessage("Completed Path!", MessageType.DEBUG)
          PathExecutor.stop()
        }

        return
      }

      MovementStatus.UNREACHED -> {
        ChatUtils.sendSystemMessage("Moving towards node [$currPathIndex]!", MessageType.DEBUG)

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
        ChatUtils.sendSystemMessage("<red>Recalculating path...</red>", MessageType.DEBUG)
        PathExecutor.changeState(CalculatingState())
      }
    }
  }

}
