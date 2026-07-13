package org.cobalt.util.rotation.data

import kotlin.math.roundToInt
import org.cobalt.util.client.PlayerUtils
import org.cobalt.util.rotation.RotationMath

data class Rotation(
  val yaw: Float,
  val pitch: Float,
  val isNormalized: Boolean = false,
) {

  fun normalize(
    currentRotation: Rotation = PlayerUtils.rotation,
  ): Rotation {
    if (isNormalized) {
      return this
    }

    val gcd = RotationMath.gcd
    val diff = currentRotation.rotationDeltaTo(this)

    val g1 = (diff.deltaYaw / gcd).roundToInt() * gcd
    val g2 = (diff.deltaPitch / gcd).roundToInt() * gcd

    val yaw = currentRotation.yaw + g1.toFloat()
    val pitch = currentRotation.pitch + g2.toFloat()

    return Rotation(yaw, pitch.coerceIn(-90f, 90f), isNormalized = true)
  }

  fun rotationDeltaTo(other: Rotation): RotationDelta {
    return RotationDelta(
      RotationMath.angleDifference(other.yaw, this.yaw),
      RotationMath.angleDifference(other.pitch, this.pitch)
    )
  }

  companion object {
    val ZERO = Rotation(0f, 0f, true)
  }

}
