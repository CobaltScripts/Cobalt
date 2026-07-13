package org.cobalt.pathfinder.state.calculation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.cobalt.pathfinder.PathFindingConfig
import org.cobalt.pathfinder.PathFindingFacade
import org.cobalt.pathfinder.calculate.path.AStarPathfinder
import org.cobalt.pathfinder.state.ExecutorState
import org.cobalt.pathfinder.state.pathing.PathingState
import org.cobalt.util.ChatUtils

class CalculatingState : ExecutorState() {
  private var calculationJob: Job? = null

  override fun enter() {
    val goal = PathFindingFacade.currentGoal
    val availableMovements = PathFindingFacade.availableMovements

    if (goal == null) {
      ChatUtils.sendSystemMessage("<red>Cannot calculate path, no goal set!</red>")
      PathFindingFacade.stop()
      return
    }

    if (availableMovements == null) {
      ChatUtils.sendSystemMessage("<red>Cannot calculate path, no movements set!</red>")
      PathFindingFacade.stop()
      return
    }

    calculationJob = CoroutineScope(Dispatchers.Default).launch {
      val path = AStarPathfinder.findPath(
        goal,
        availableMovements,
        PathFindingConfig.returnBestNode
      )

      if (!isActive) return@launch

      if (path == null) {
        ChatUtils.sendSystemMessage("<red>Unable to find a path</red>")
        PathFindingFacade.stop()
        return@launch
      }

      PathFindingFacade.path = path
      PathFindingFacade.changeState(PathingState())
    }
  }

  override fun exit() {
    calculationJob?.cancel()
    calculationJob = null
  }
}
