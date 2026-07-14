package org.cobalt.pathfinder.state

import org.cobalt.pathfinder.PathExecutor
import org.cobalt.pathfinder.helper.MovementController

abstract class ExecutorState {

  protected val movementController: MovementController =
    PathExecutor.movementController

  open fun enter() {}
  open fun onTick() {}
  open fun onRender() {}
  open fun exit() {}

}
