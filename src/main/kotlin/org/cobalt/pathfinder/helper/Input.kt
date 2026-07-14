package org.cobalt.pathfinder.helper

data class Input(
  var forward: Boolean = false,
  var backward: Boolean = false,
  var left: Boolean = false,
  var right: Boolean = false,
  var jump: Boolean = false,
  var sprint: Boolean = false,
  var sneak: Boolean = false,
) {

  fun apply(other: Input) {
    forward = forward || other.forward
    backward = backward || other.backward
    left = left || other.left
    right = right || other.right
    jump = jump || other.jump
    sprint = sprint || other.sprint
    sneak = sneak || other.sneak
  }

}
