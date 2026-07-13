package org.cobalt.pathfinder.state.impl

import org.cobalt.pathfinder.PathExecutor
import org.cobalt.pathfinder.PathFinderConfig
import org.cobalt.pathfinder.calculate.path.AStarPathfinder
import org.cobalt.pathfinder.state.ExecutorState
import org.cobalt.util.ChatUtils
import org.cobalt.util.MessageType
import org.cobalt.util.PlayerUtils
import org.cobalt.util.helper.Multithreading

class CalculatingState : ExecutorState() {

  override fun enter() {
    val startPos = PlayerUtils.position
    val goal = PathExecutor.currentGoal
    val availableMovements = PathExecutor.availableMovements

    if (goal == null) {
      ChatUtils.sendSystemMessage("<red>Cannot calculate path, no goal set!</red>")
      PathExecutor.stop()
      return
    }

    if (availableMovements == null) {
      ChatUtils.sendSystemMessage("<red>Cannot calculate path, no movements set!</red>")
      PathExecutor.stop()
      return
    }

    val pathFinder = AStarPathfinder(
      startPos.x, startPos.y, startPos.z,
      goal,
      availableMovements,
      PathFinderConfig.returnBestNode
    )

    Multithreading.runAsync {
      val path = pathFinder.findPath()

      if (path == null) {
        ChatUtils.sendSystemMessage("<red>Unable to find a path</red>")
        PathExecutor.stop()
        return@runAsync
      }

      PathExecutor.path = path
      PathExecutor.changeState(PathingState())

      ChatUtils.sendSystemMessage(
        "Found ${path.nodes.size} node path in ${path.timeElapsed.inWholeMilliseconds}ms",
        MessageType.DEBUG
      )
    }
  }

}
