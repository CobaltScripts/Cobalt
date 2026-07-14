package org.cobalt.pathfinder.movement

import net.minecraft.world.phys.Vec3
import org.cobalt.pathfinder.helper.PlayerInput

class MovementState(
  val target: MovementTarget? = null,
  val status: MovementStatus = MovementStatus.UNREACHED,
)

data class MovementTarget(
  val input: PlayerInput = PlayerInput(),
  val lookAt: Vec3? = null,
)

enum class MovementStatus {
  REACHED, UNREACHED, FAILED
}

enum class MovementType {
  WALK, FLY
}
