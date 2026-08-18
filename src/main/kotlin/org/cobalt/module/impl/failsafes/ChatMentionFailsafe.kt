package org.cobalt.module.impl.failsafes

import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.PacketEvent
import org.cobalt.module.ModuleManager
import org.cobalt.module.type.Failsafe
import org.cobalt.ui.component.setting.impl.CheckboxSetting
import org.cobalt.ui.component.setting.impl.ModeSetting
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.chat.MessageType
import org.cobalt.util.client.PlayerUtils
import org.cobalt.util.scheduling.TickScheduler

object ChatMentionFailsafe: Failsafe("Chat Mention", 10, false) {
  private var respond by CheckboxSetting("Respond to chat messages", "respond", false)
  private var listOfResponses = listOf(
    "?",
    "wha",
    "what",
    "ig", // TODO: add gui thing for this, idk how id do it so ill leave it as this
    "i guess",
    "waht",
  )

  @SubscribeEvent
  fun onChatMessage(event: PacketEvent.Receive) {
    val packet = event.packet as? ClientboundSystemChatPacket ?: return

    val chatMessage = packet.content.string
    val sender = getSenderFromComponent(packet.content)

    if (chatMessage.contains(PlayerUtils.ign) && sender != PlayerUtils.ign) {
      if (respond) {
        performReaction()
      }
      ChatUtils.sendSystemMessage("You were mentioned in chat by $sender!", MessageType.FAILSAFE)
    }
  }

  private fun getSenderFromComponent(component: Component): String? {
    val text = component.string

    val i = text.indexOf(":")
    if (i == -1) return null

    val bc = text.substring(0,i)

    return bc
      .substringAfterLast(']')
      .removePrefix("From ")
      .trim()
      .ifEmpty { null }
  }

  override fun resetStates() {
    TODO("Not yet implemented")
  }

  override fun performReaction(): ReactionResult? {
    if (!respond) return null
    val currScript = ModuleManager.currentScript ?: return null

    currScript.pause()

    TickScheduler.schedule(23L, Runnable {
      ChatUtils.sendSystemMessage("should reply", MessageType.DEBUG)
      ChatUtils.sendPlayerMessage(listOfResponses.random())
      currScript.resume()
    })

    return ReactionResult.FINISHED
  }

}
