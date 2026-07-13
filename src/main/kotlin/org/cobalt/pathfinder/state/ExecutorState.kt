package org.cobalt.pathfinder.state

import org.cobalt.pathfinder.PathFindingFacade
import org.cobalt.pathfinder.PathInput

abstract class ExecutorState {

  protected val input: PathInput =
    PathFindingFacade.pathInput

  open fun enter() {}
  open fun onTick() {}
  open fun onRender() {}
  open fun exit() {}

}
