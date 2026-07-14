package org.cobalt.pathfinder.state

import org.cobalt.pathfinder.PathExecutor
import org.cobalt.pathfinder.helper.PlayerInput

abstract class ExecutorState {

  protected val playerInput: PlayerInput =
    PathExecutor.playerInput

  open fun enter() {}
  open fun onTick() {}
  open fun onRender() {}
  open fun exit() {}

}
