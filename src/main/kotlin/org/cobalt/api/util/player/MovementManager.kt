package org.cobalt.api.util.player

object MovementManager {

  /**
   * When false, player can control the camera, but if true, the player can't control the camera.
   */
  @JvmField
  @Volatile
  var isLookLocked = false

  /**
  * Updates the `isLookLocked` state.
  *
  * @param state The new lock state. Defaults to `true`.
  */
  fun setLookLock(state: Boolean = true) {
    isLookLocked = state
  }

}
