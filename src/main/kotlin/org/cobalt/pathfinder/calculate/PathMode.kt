package org.cobalt.pathfinder.calculate

import org.cobalt.pathfinder.movement.Movement
import org.cobalt.pathfinder.movement.walk.MovementAscend
import org.cobalt.pathfinder.movement.walk.MovementDescend
import org.cobalt.pathfinder.movement.walk.MovementDiagonal
import org.cobalt.pathfinder.movement.walk.MovementTraverse

enum class PathMode(vararg val movements: Movement) {
  WALK(
    MovementTraverse.NORTH,
    MovementTraverse.SOUTH,
    MovementTraverse.EAST,
    MovementTraverse.WEST,

    MovementDiagonal.NORTH_EAST,
    MovementDiagonal.NORTH_WEST,
    MovementDiagonal.SOUTH_EAST,
    MovementDiagonal.SOUTH_WEST,

    MovementAscend.NORTH,
    MovementAscend.SOUTH,
    MovementAscend.EAST,
    MovementAscend.WEST,

    MovementDescend.NORTH,
    MovementDescend.SOUTH,
    MovementDescend.EAST,
    MovementDescend.WEST
  ),

  FLY
}
