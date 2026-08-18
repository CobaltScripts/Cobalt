package org.cobalt.module.impl.failsafes

import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket
import org.cobalt.Cobalt
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.PacketEvent
import org.cobalt.module.type.Failsafe
import org.cobalt.util.failsafe.FailsafeManager
import org.cobalt.util.inventory.InventoryUtils

object SlotChangeFailsafe: Failsafe("Slot Change", 10, true) {
  @SubscribeEvent
  fun onServerItemChange(event: PacketEvent.Receive) {
    if (Cobalt.minecraft.player == null) return
    if (event.packet !is ClientboundSetHeldSlotPacket) return
    val oldSlot = Cobalt.minecraft.player?.inventory?.selectedSlot
    val newSlot = event.packet.slot ?: return // this should never return? idk

    if (oldSlot == newSlot) return

    FailsafeManager.alertUser(this, "FROM SLOT $oldSlot TO SLOT $newSlot")
  }

  override fun resetStates() {
    TODO("Not yet implemented")
  }

  override fun performReaction(): ReactionResult {
    TODO("Not yet implemented")
  }

}
