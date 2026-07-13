package org.cobalt.pathfinder.state.impl

import org.cobalt.pathfinder.PathFindingFacade
import org.cobalt.pathfinder.helper.PathRenderer
import org.cobalt.pathfinder.state.ExecutorState
import org.cobalt.util.PlayerUtils

class StartFlyState : ExecutorState() {

  private var flyStage = 0

  override fun enter() {
    flyStage = 0
  }

  override fun onTick() {
    if (handleFlyStart()) {
      return
    }

    PathFindingFacade.changeState(PathingState())
  }

  override fun onRender() {
    PathRenderer.render()
  }

  private fun handleFlyStart(): Boolean {
    if (PlayerUtils.isFlying) {
      flyStage = 0
      return false
    }

    if (PlayerUtils.onGround) {
      flyStage = 0
    }

    input.stopMovement()

    when (flyStage) {
      0 -> {
        input.jump = true
        flyStage = 1
      }

      1 -> {
        input.jump = false
        flyStage = 2
      }

      2 -> {
        input.jump = true
        flyStage = 3
      }

      3 -> {
        input.jump = false
      }
    }

    return true
  }

}
