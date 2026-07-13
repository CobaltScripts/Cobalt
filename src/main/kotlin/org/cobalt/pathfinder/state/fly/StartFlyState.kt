package org.cobalt.pathfinder.state.fly

import org.cobalt.pathfinder.PathExecutor
import org.cobalt.pathfinder.state.ExecutorState
import org.cobalt.pathfinder.state.pathing.PathingState
import org.cobalt.util.client.PlayerUtils

class StartFlyState : ExecutorState() {

  private var flyStage = FlyStage.INITIAL_JUMP

  override fun enter() {
    flyStage = FlyStage.INITIAL_JUMP
  }

  override fun onTick() {
    if (handleFlyStart()) {
      return
    }

    PathExecutor.changeState(PathingState())
  }

  private fun handleFlyStart(): Boolean {
    if (PlayerUtils.isFlying) {
      flyStage = FlyStage.INITIAL_JUMP
      return false
    }

    if (PlayerUtils.onGround) {
      flyStage = FlyStage.INITIAL_JUMP
    }

    input.stopMovement()

    when (flyStage) {
      FlyStage.INITIAL_JUMP -> {
        input.jump = true
        flyStage = FlyStage.RELEASE_INITIAL_JUMP
      }

      FlyStage.RELEASE_INITIAL_JUMP -> {
        input.jump = false
        flyStage = FlyStage.SECOND_JUMP
      }

      FlyStage.SECOND_JUMP -> {
        input.jump = true
        flyStage = FlyStage.RELEASE_SECOND_JUMP
      }

      FlyStage.RELEASE_SECOND_JUMP -> {
        input.jump = false
      }
    }

    return true
  }

  private enum class FlyStage {
    INITIAL_JUMP,
    RELEASE_INITIAL_JUMP,
    SECOND_JUMP,
    RELEASE_SECOND_JUMP,
  }

}
