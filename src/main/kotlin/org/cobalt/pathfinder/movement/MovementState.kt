package org.cobalt.pathfinder.movement

import net.minecraft.world.phys.Vec3
import org.cobalt.pathfinder.helper.PlayerInput

class MovementState(
  val status: MovementStatus,
  val target: MovementTarget? = null,
)

data class MovementTarget(
  val input: PlayerInput = PlayerInput(),
  val lookAt: Vec3? = null,
)

enum class MovementStatus {
  REACHED, UNREACHED
}

enum class MovementType {
  WALK, FLY
}
