package org.cobalt.pathfinder.state.impl

import org.cobalt.pathfinder.PathFindingFacade
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
        PathFindingFacade.stop()
        return@runAsync
      }

      PathFindingFacade.path = path
      PathFindingFacade.changeState(PathingState())

      ChatUtils.sendSystemMessage(
        "Found ${path.nodes.size} node path in ${path.timeElapsed.inWholeMilliseconds}ms",
        MessageType.DEBUG
      )
    }
  }

}
