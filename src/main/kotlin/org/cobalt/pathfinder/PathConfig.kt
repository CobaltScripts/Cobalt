package org.cobalt.pathfinder

import org.cobalt.pathfinder.calculate.PathMode
import org.cobalt.pathfinder.goal.Goal
import org.cobalt.pathfinder.movement.Movement

class PathConfig(
  val goal: Goal,
  val movements: Array<out Movement> = PathMode.WALK.movements,
  val shouldSprint: Boolean = true,
  val preferShifting: Boolean = false,
  val returnBestNode: Boolean = false,
) {

  val useFlyMovement = movements.any {
    it.type == Movement.Type.FLY
  }

}
