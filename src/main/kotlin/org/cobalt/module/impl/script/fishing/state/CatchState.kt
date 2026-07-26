package org.cobalt.module.impl.script.fishing.state

import kotlin.random.Random
import net.minecraft.world.entity.decoration.ArmorStand
import org.cobalt.module.impl.misc.Rotations
import org.cobalt.module.impl.script.ScriptState
import org.cobalt.module.impl.script.fishing.FishingScript
import org.cobalt.util.input.Mouse
import org.cobalt.util.rotation.RotationMath

class CatchState : ScriptState() {

  private var caught = false

  override fun onTick() {
    if (!caught) {
      if (FishingScript.antiAfkDelay.passed()) {
        FishingScript.lookPos?.add(
          Random.nextDouble(-0.25, 0.25),
          Random.nextDouble(-0.25, 0.25),
          Random.nextDouble(-0.25, 0.25)
        )?.let {
          Rotations.start(RotationMath.getRotation(it))
        }

        FishingScript.antiAfkDelay.schedule(Random.nextLong(10_000, 15_000))
      }

      if (!detectFishBite()) {
        return
      }

      caught = true
      FishingScript.globalDelay.schedule(Random.nextLong(100, 150))
      return
    }

    Mouse.rightClick()
    FishingScript.globalDelay.schedule(Random.nextLong(300, 350))
    FishingScript.changeState(CastRodState(FishingScript.rodSlot))
  }

  private fun detectFishBite(): Boolean {
    return minecraft.level
      ?.entitiesForRendering()
      ?.any {
        it is ArmorStand && it.customName?.string == "!!!"
      } ?: false
  }

}
