package org.cobalt.internal.rotation

import net.minecraft.client.network.ClientPlayerEntity

interface IRotationStrategy {

  /**
   * Called every frame to handle rotation logic for this strategy.
   *
   * @param player The client player entity
   * @param targetYaw The target yaw rotation
   * @param targetPitch The target pitch rotation
   * @return Pair of (newYaw, newPitch) or null if rotation is complete
   */
  fun onRotate(
    player: ClientPlayerEntity,
    targetYaw: Float,
    targetPitch: Float,
  ): Pair<Float, Float>?

  /**
   * Called when the rotation strategy is started.
   */
  fun onStart() {}

  /**
   * Called when the rotation strategy is stopped.
   */
  fun onStop() {}

}
