package org.cobalt.pathfinder

import net.minecraft.client.player.ClientInput
import net.minecraft.world.entity.player.Input
import net.minecraft.world.phys.Vec2
import org.cobalt.pathfinder.helper.PlayerInput

class PathInput : ClientInput() {

  var forward = false
  var backward = false
  var left = false
  var right = false
  var jump = false
  var shift = false
  var sprint = false

  fun applyInput(input: PlayerInput) {
    forward = input.forward
    backward = input.backward
    left = input.left
    right = input.right
    jump = input.jump
    sprint = input.sprint
    shift = input.sneak
  }

  fun stopMovement() {
    applyInput(PlayerInput())
  }

  override fun tick() {
    this.keyPresses = Input(
      forward,
      backward,
      left,
      right,
      jump,
      shift,
      sprint
    )

    val forwardImpulse = calculateImpulse(forward, backward)
    val leftImpulse = calculateImpulse(left, right)
    this.moveVector = Vec2(leftImpulse, forwardImpulse).normalized()
  }

  private fun calculateImpulse(positive: Boolean, negative: Boolean): Float {
    return when {
      positive == negative -> 0.0f
      positive -> 1.0f
      else -> -1.0f
    }
  }

}
