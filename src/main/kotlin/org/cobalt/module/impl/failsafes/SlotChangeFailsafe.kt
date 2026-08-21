package org.cobalt.module.impl.failsafes

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket
import org.cobalt.Cobalt
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.PacketEvent
import org.cobalt.module.ModuleManager
import org.cobalt.module.type.Failsafe
import org.cobalt.util.failsafe.FailsafeManager
import org.cobalt.util.inventory.InventoryUtils

object SlotChangeFailsafe: Failsafe("Slot Change", 10, false) {
  @SubscribeEvent
  fun onServerItemChange(event: PacketEvent.Receive) {
    if (!ModuleManager.isScriptRunning() && !FabricLoader.getInstance().isDevelopmentEnvironment) return
    val player = Cobalt.minecraft.player ?: return
    val packet = event.packet as? ClientboundSetHeldSlotPacket ?: return

    val oldSlot = player.inventory.selectedSlot
    val newSlot = packet.slot

    if (oldSlot == newSlot) return

    FailsafeManager.alertUser(this,
      "<red>FROM SLOT</red>" +
      " <yellow>$oldSlot</yellow>" +
      " <red>TO SLOT</red>" +
      " <yellow>$newSlot</yellow>"
    )
  }

  override fun resetStates() {
    TODO("Not yet implemented")
  }

  override fun performReaction(): ReactionResult {
    TODO("Not yet implemented")
  }

}
