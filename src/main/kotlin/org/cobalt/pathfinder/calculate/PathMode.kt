package org.cobalt.pathfinder.calculate

import org.cobalt.pathfinder.movement.Movement
import org.cobalt.pathfinder.movement.walk.AscendMovement
import org.cobalt.pathfinder.movement.walk.DescendMovement
import org.cobalt.pathfinder.movement.walk.DiagonalMovement
import org.cobalt.pathfinder.movement.walk.TraverseMovement

enum class PathMode(vararg val movements: Movement) {
  WALK(
    TraverseMovement.NORTH,
    TraverseMovement.SOUTH,
    TraverseMovement.EAST,
    TraverseMovement.WEST,

    DiagonalMovement.NORTH_EAST,
    DiagonalMovement.NORTH_WEST,
    DiagonalMovement.SOUTH_EAST,
    DiagonalMovement.SOUTH_WEST,

    AscendMovement.NORTH,
    AscendMovement.SOUTH,
    AscendMovement.EAST,
    AscendMovement.WEST,

    DescendMovement.NORTH,
    DescendMovement.SOUTH,
    DescendMovement.EAST,
    DescendMovement.WEST
  ),

  FLY
}
