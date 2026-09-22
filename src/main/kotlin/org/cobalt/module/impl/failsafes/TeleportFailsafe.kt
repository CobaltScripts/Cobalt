package org.cobalt.module.impl.failsafes

import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket
import net.minecraft.network.protocol.game.ServerboundUseItemPacket
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.PacketEvent
import org.cobalt.module.type.Failsafe
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.chat.MessageType
import org.cobalt.util.failsafe.FailsafeManager
import org.cobalt.util.inventory.InventoryUtils

object TeleportFailsafe : Failsafe("Teleport", 10, false) {
  @SubscribeEvent
  fun onTeleport(event: PacketEvent.Any) {
    if (!shouldReactToEvents()) return
    if (minecraft.level == null) return

    when (val packet = event.packet) {
      is ClientboundPlayerPositionPacket -> handlePositionPacket(packet)
      is ServerboundChatCommandPacket -> handleChatCommandPacket(packet)
      is ServerboundUseItemPacket -> handleUseItemPacket()
    }
  }

  private fun handlePositionPacket(packet: ClientboundPlayerPositionPacket) {
    val oldBP = try {
      BlockPos(
        minecraft.player!!.x.toInt(),
        minecraft.player!!.y.toInt(),
        minecraft.player!!.z.toInt()
      )
    } catch (_: NullPointerException) {
      return
    }

    val newBlockPosition = BlockPos(
      packet.change.position.x.toInt(),
      packet.change.position.y.toInt(),
      packet.change.position.z.toInt()
    )

    if (newBlockPosition.x == 0 && newBlockPosition.y == 0 && newBlockPosition.z == 0) {
      ChatUtils.sendSystemMessage("ignoring teleport failsafe (0,0,0 pos)", MessageType.FAILSAFE)
      return
    }

    FailsafeManager.alertUser(
      this,
      "<red>FROM</red> <yellow>$oldBP</yellow>" +
        " <red>TO</red>" +
        " <yellow>$newBlockPosition</yellow>"
    )
  }

  private fun handleChatCommandPacket(packet: ServerboundChatCommandPacket) {
    if (!packet.command.contains("warp")) return
    FailsafeManager.ignoreFailsafe(this)
  }

  private fun handleUseItemPacket() {
    val aotvSlot = InventoryUtils.findItemInHotbarRegex(
      Regex("""(Ender Pearl|Aspect Of The (End|Void))\b""", RegexOption.IGNORE_CASE)
    )

    if (aotvSlot == -1) return

    val player = minecraft.player ?: return

    if (player.inventory.selectedSlot == aotvSlot) {
      FailsafeManager.ignoreFailsafe(this)
      FailsafeManager.ignoreFailsafe(RotationFailsafe)
    }
  }

  override fun resetStates() {
    return
  }

  override fun performReaction(): ReactionResult? {
    // TODO
    return null
  }

}
