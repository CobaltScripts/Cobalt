package org.cobalt.pathfinder.state.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.cobalt.pathfinder.PathExecutor
import org.cobalt.pathfinder.calculate.path.AStarPathfinder
import org.cobalt.pathfinder.state.ExecutorState
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.client.PlayerUtils

class CalculatingState : ExecutorState() {

  private var calculationJob: Job? = null

  override fun enter() {
    val config = PathExecutor.config ?: run {
      ChatUtils.sendSystemMessage("<red>Cannot calculate path, no path config set!</red>")
      PathExecutor.stop()
      return
    }

    val startPos = PlayerUtils.blockStandingOn
    val goal = config.goal

    calculationJob = CoroutineScope(Dispatchers.Default).launch {
      val path = AStarPathfinder(
        startPos.x, startPos.y, startPos.z,
        goal, config.mode,
        config.calculationContext,
        config.returnBestNode,
        config.maxCalculationTime
      ).findPath()

      if (!isActive) {
        return@launch
      }

      if (path == null) {
        ChatUtils.sendSystemMessage("<red>Unable to find a path</red>")
        PathExecutor.stop()
        return@launch
      }

      PathExecutor.path = path
      PathExecutor.changeState(PathingState())
    }
  }

  override fun exit() {
    calculationJob?.cancel()
    calculationJob = null
  }

}
