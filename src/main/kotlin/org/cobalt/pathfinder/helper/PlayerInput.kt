package org.cobalt.pathfinder.helper

data class PlayerInput(
  var forward: Boolean = false,
  var backward: Boolean = false,
  var left: Boolean = false,
  var right: Boolean = false,
  var jump: Boolean = false,
  var sprint: Boolean = false,
  var sneak: Boolean = false,
) {

  fun mergeFrom(other: PlayerInput) {
    forward = forward || other.forward
    backward = backward || other.backward
    left = left || other.left
    right = right || other.right
    jump = jump || other.jump
    sprint = sprint || other.sprint
    sneak = sneak || other.sneak
  }

  operator fun plus(other: PlayerInput): PlayerInput =
    copy().also { it.mergeFrom(other) }

  operator fun plusAssign(other: PlayerInput) {
    mergeFrom(other)
  }

}
