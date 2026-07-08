package org.cobalt.util

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import org.cobalt.Cobalt.minecraft
import org.cobalt.util.rotation.Rotation


object RotationUtils {

  @JvmStatic
  val gcd: Double
    get() = (mouseSensitivityFactor.toFloat() * MOUSE_TURN_SCALE).toDouble()

  @JvmStatic
  val mouseSensitivityFactor: Double
    get() {
      val sensitivity = minecraft.options.sensitivity().get()
      val f = sensitivity * 0.6f + 0.2f
      return f * f * f * 8.0
    }

  @JvmStatic
  fun angleDifference(a: Float, b: Float): Float {
    return Mth.wrapDegrees(a - b)
  }

  @JvmStatic
  fun approximatelyEquals(current: Rotation, other: Rotation, tolerance: Float = 2f): Boolean {
    val deltaYaw = angleDifference(other.yaw, current.yaw)
    val deltaPitch = angleDifference(other.pitch, current.pitch)
    return abs(deltaYaw) <= tolerance && abs(deltaPitch) <= tolerance
  }

  @JvmStatic
  fun getRotation(start: Vec3, end: Vec3): Rotation {
    val dx = end.x - start.x
    val dy = end.y - start.y
    val dz = end.z - start.z

    val horizontalDistance = sqrt(dx * dx + dz * dz)

    val yaw = (atan2(dz, dx) * RAD_TO_DEG).toFloat() - 90f
    val pitch = (-(atan2(dy, horizontalDistance) * RAD_TO_DEG)).toFloat()

    return Rotation(yaw, pitch)
  }

  @JvmStatic
  fun getRotation(end: Vec3): Rotation {
    val start = minecraft.player?.eyePosition ?: return Rotation.ZERO
    return getRotation(start, end)
  }

  const val RAD_TO_DEG: Double = 180.0 / Math.PI
  private const val MOUSE_TURN_SCALE = 0.15f

}
