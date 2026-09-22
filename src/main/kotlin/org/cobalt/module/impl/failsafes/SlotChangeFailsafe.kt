package org.cobalt.module.impl.failsafes

import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket
import org.cobalt.Cobalt
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.PacketEvent
import org.cobalt.module.type.Failsafe
import org.cobalt.util.failsafe.FailsafeManager

object SlotChangeFailsafe : Failsafe("Slot Change", 10, false) {
  @SubscribeEvent
  fun onServerItemChange(event: PacketEvent.Receive) {
    if (!shouldReactToEvents()) return
    val player = Cobalt.minecraft.player ?: return
    val packet = event.packet as? ClientboundSetHeldSlotPacket ?: return

    val oldSlot = player.inventory.selectedSlot
    val newSlot = packet.slot

    if (oldSlot == newSlot) return

    FailsafeManager.alertUser(
      this,
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
