package org.cobalt.internal.rotation

import net.minecraft.client.network.ClientPlayerEntity
import org.cobalt.api.util.player.MovementManager
import org.cobalt.api.util.rotation.IRotationExec

object RotationExec : IRotationExec {

  private var targetYaw: Float = 0F
  private var targetPitch: Float = 0F

  private var currStrat: IRotationStrategy? = null
  private var isRotating: Boolean = false

  fun rotateTo(
    yaw: Float,
    pitch: Float,
    strategy: IRotationStrategy
  ) {
    stopRotating()

    targetYaw = yaw
    targetPitch = pitch
    currStrat = strategy

    strategy.onStart()
    MovementManager.setLookLock(true)
    isRotating = true
  }

  fun stopRotating() {
    currStrat?.onStop()
    currStrat = null
    isRotating = false
    MovementManager.setLookLock(false)
  }

  override fun onRotate(
    player: ClientPlayerEntity
  ) {
    if (!isRotating) {
      return
    }

    currStrat?.let {
      val result = it.onRotate(
        player,
        targetYaw,
        targetPitch
      )

      if (result == null) {
        stopRotating()
      } else {
        player.yaw = result.first
        player.pitch = result.second
      }
    }
  }

}
