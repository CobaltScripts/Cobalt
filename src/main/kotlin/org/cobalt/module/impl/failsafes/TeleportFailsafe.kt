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
import org.cobalt.util.client.PlayerUtils.player
import org.cobalt.util.failsafe.FailsafeManager
import org.cobalt.util.inventory.InventoryUtils
import org.cobalt.util.inventory.ItemUtils
import org.cobalt.util.rotation.data.Rotation

object TeleportFailsafe: Failsafe("Teleport", 10, true) {
  @SubscribeEvent
  fun onTeleport(event: PacketEvent.Any) {
    if (minecraft.level == null) return
    when (val packet = event.packet) {
      is ClientboundPlayerPositionPacket -> {
        if (player == null) return

        val currentRot = Rotation(player!!.yRot, player!!.xRot)
        val newRot = Rotation(packet.change.yRot, packet.change.xRot)

        if (currentRot != newRot) {
          RotationFailsafe.onRotation(currentRot, newRot)
        } // I'll be honest I'm not entirely sure what else to do here since they both use the same packet?
          // this will indeed flag both teleport and rotation on rotation check rn i can't think of a way to
          // distinguish them (tired zzz)

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

        if (newBP.x == 0 && newBP.y == 0 && newBP.z == 0) {
          ChatUtils.sendSystemMessage("ignoring teleport failsafe (0,0,0 pos)", MessageType.FAILSAFE)
          return
        }

        FailsafeManager.alertUser(
          this,
          "<red>FROM</red> <yellow>$oldBP</yellow>" +
          " <red>TO</red>" +
          " <yellow>$newBP</yellow>"
        )
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
