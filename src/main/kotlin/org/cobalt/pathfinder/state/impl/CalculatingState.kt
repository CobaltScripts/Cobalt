package org.cobalt.pathfinder.state.impl

import org.cobalt.pathfinder.PathExecutor
import org.cobalt.pathfinder.calculate.path.AStarPathfinder
import org.cobalt.pathfinder.state.ExecutorState
import org.cobalt.util.ChatUtils
import org.cobalt.util.MessageType
import org.cobalt.util.PlayerUtils
import org.cobalt.util.helper.Multithreading

class CalculatingState : ExecutorState() {

  override fun enter() {
    val config = PathExecutor.config ?: return
    val startPos = PlayerUtils.position

    val pathFinder = AStarPathfinder(
      startPos.x, startPos.y, startPos.z,
      config.goal, config.movements,
      config.returnBestNode
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
