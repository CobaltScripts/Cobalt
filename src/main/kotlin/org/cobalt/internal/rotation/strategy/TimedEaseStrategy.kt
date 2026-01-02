package org.cobalt.internal.rotation.strategy

import net.minecraft.client.network.ClientPlayerEntity
import org.cobalt.internal.rotation.EasingType
import org.cobalt.internal.rotation.IRotationStrategy

class TimedEaseStrategy(
  private val yawEaseType: EasingType = EasingType.LINEAR,
  private val pitchEaseType: EasingType = EasingType.LINEAR,
  private val duration: Long = 500L
) : IRotationStrategy {

  private var startTime: Long = 0L
  private var startYaw: Float? = null
  private var startPitch: Float? = null

  override fun onStart() {
    startTime = System.currentTimeMillis()
    startYaw = null
    startPitch = null
  }

  override fun onRotate(
    player: ClientPlayerEntity,
    targetYaw: Float,
    targetPitch: Float,
  ): Pair<Float, Float>? {
    if (startYaw == null || startPitch == null) {
      startYaw = player.yaw
      startPitch = player.pitch
    }

    val elapsed = System.currentTimeMillis() - startTime
    val progress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)

    if (progress >= 1f) {
      return null
    }

    val newYaw = yawEaseType.apply(startYaw!!, targetYaw, progress)
    val newPitch = pitchEaseType.apply(startPitch!!, targetPitch, progress)

    return Pair(newYaw, newPitch)
  }

}
