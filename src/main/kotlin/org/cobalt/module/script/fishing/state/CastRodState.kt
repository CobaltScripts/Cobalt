package org.cobalt.module.script.fishing.state

import kotlin.random.Random
import org.cobalt.module.script.ScriptState
import org.cobalt.module.script.fishing.FishingScript
import org.cobalt.util.input.Mouse
import org.cobalt.util.inventory.InventoryUtils

class CastRodState(slot: Int, var castRod: Boolean = true) : ScriptState() {

  private val rodSlot = slot - 1
  private var holdRod = false

  override fun enter() {
    holdRod = minecraft.player?.inventory?.selectedSlot != rodSlot
  }

  override fun onTick() {
    if (holdRod) {
      InventoryUtils.selectHotbarSlot(rodSlot)
      FishingScript.globalDelay.schedule(Random.nextLong(200, 250))
      holdRod = false
      return
    }

    if (castRod) {
      Mouse.rightClick()
    }

    FishingScript.changeState(CatchState())
  }


}
