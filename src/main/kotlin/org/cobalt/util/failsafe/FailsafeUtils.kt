package org.cobalt.util.failsafe

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import org.cobalt.module.impl.failsafes.RotationFailsafe
import org.cobalt.module.impl.failsafes.SlotChangeFailsafe
import org.cobalt.module.impl.failsafes.TeleportFailsafe
import org.cobalt.module.type.Failsafe

object FailsafeUtils {
  fun init() {
    ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
      val falseFlagOnWorldChange = listOf<Failsafe>(
        TeleportFailsafe,
        SlotChangeFailsafe,
        RotationFailsafe
      )

      falseFlagOnWorldChange.forEach {
        FailsafeManager.ignoreFailsafe(it)
      }
    }
  }
}
