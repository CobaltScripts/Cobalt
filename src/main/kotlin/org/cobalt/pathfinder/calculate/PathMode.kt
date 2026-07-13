package org.cobalt.pathfinder.calculate

import org.cobalt.pathfinder.movement.Movement
import org.cobalt.pathfinder.movement.fly.FlyAscendMovement
import org.cobalt.pathfinder.movement.fly.FlyDescendMovement
import org.cobalt.pathfinder.movement.fly.FlyDiagonalMovement
import org.cobalt.pathfinder.movement.fly.FlyTraverseMovement
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

  FLY(
    FlyTraverseMovement.NORTH,
    FlyTraverseMovement.SOUTH,
    FlyTraverseMovement.EAST,
    FlyTraverseMovement.WEST,

    FlyDiagonalMovement.NORTH_EAST,
    FlyDiagonalMovement.NORTH_WEST,
    FlyDiagonalMovement.SOUTH_EAST,
    FlyDiagonalMovement.SOUTH_WEST,

    FlyAscendMovement.DEFAULT,
    FlyDescendMovement.DEFAULT,
  )
}
