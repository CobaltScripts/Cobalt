package org.cobalt.module.impl.failsafes

import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket
import net.minecraft.network.protocol.game.ServerboundUseItemPacket
import org.cobalt.Cobalt.minecraft
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.PacketEvent
import org.cobalt.module.type.Failsafe
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.chat.MessageType
import org.cobalt.util.failsafe.FailsafeManager
import org.cobalt.util.inventory.InventoryUtils
import org.cobalt.util.inventory.ItemUtils

object TeleportFailsafe: Failsafe("Teleport", 10, true) {
  @SubscribeEvent
  fun onTeleport(event: PacketEvent.Any) {
    when (val packet = event.packet) {
      is ClientboundPlayerPositionPacket -> {
        val oldBP
          : BlockPos = BlockPos(
          minecraft.player!!.x.toInt(),
          minecraft.player!!.y.toInt(),
          minecraft.player!!.z.toInt()
        )

        val newBP
          : BlockPos = BlockPos(
          event.packet.change.position.x.toInt(),
          event.packet.change.position.y.toInt(),
          event.packet.change.position.z.toInt()
        )

        FailsafeManager.alertUser(this, "FROM $oldBP TO $newBP")
      }

      is ServerboundChatCommandPacket -> {
        if (!packet.command.contains("warp")) return
        FailsafeManager.ignoreFailsafe(this)
      }

      is ServerboundUseItemPacket -> {
        val aotvSlot = InventoryUtils.findItemInHotbarRegex(
          Regex("""(Ender Pearl|Aspect Of The (End|Void))\b""", RegexOption.IGNORE_CASE)
        )

        if (aotvSlot == -1) return

        val player = minecraft.player ?: return

        if (player.inventory.selectedSlot == aotvSlot) {
          FailsafeManager.ignoreFailsafe(this)
        }
      }
    }
  }

  override fun resetStates() {
    TODO("Not yet implemented")
  }

  override fun performReaction(): ReactionResult {
    TODO("Not yet implemented")
  }

}
