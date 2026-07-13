package org.cobalt.pathfinder.state

import org.cobalt.pathfinder.PathExecutor
import org.cobalt.pathfinder.helper.PathInput

abstract class ExecutorState {

  protected val input: PathInput =
    PathExecutor.pathInput

  open fun enter() {}
  open fun onTick() {}
  open fun onRender() {}
  open fun exit() {}

}
