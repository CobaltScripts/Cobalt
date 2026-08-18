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
import org.cobalt.ui.component.setting.impl.TextSetting
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.chat.MessageType
import org.cobalt.util.client.PlayerUtils
import org.cobalt.util.scheduling.TickScheduler

object ChatMentionFailsafe: Failsafe("Chat Mention", 10, false) {
  private var respond by CheckboxSetting("Respond to chat messages", "respond", false)
  private var badPhrases by TextSetting(
    "Other phrases",
    "Enter words that you want to trigger the failsafe, seperated by commas",
    "wdr,macro,cheat,admin,botting"
  )

  private var listOfResponses by TextSetting(
    "Responses",
    "Enter what you want Cobalt to reply to people mentioning your name with",
    "what,?,??,???,wha,waht,wsp"
  )

  @SubscribeEvent
  fun onChatMessage(event: PacketEvent.Receive) {
    val packet = event.packet as? ClientboundSystemChatPacket ?: return

    val chatMessage = packet.content.string
    val sender = getSenderFromComponent(packet.content)
    val badWord = getBadWords().any { chatMessage.contains(it, ignoreCase = true) }
    if ((chatMessage.contains(PlayerUtils.ign) || badWord) && sender != PlayerUtils.ign) {
      if (respond && !badWord) { // incase they check someone else we wouldnt want to respond :pray:
        performReaction()
      }
      ChatUtils.sendSystemMessage("<red>Bad phrase in chat found by</red>" +
        " <yellow>$sender</yellow>!" +
        " <grey>($chatMessage)</grey>",
        MessageType.FAILSAFE
      )
    }
  }

  private fun getBadWords(): List<String> {
    return badPhrases
      .split(",")
      .map { it.trim() }
      .filter { it.isNotEmpty() }
  }

  private fun getResponses(): List<String> {
    return listOfResponses.split(',').map { it.trim() }.filter { it.isNotEmpty() }
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

    TickScheduler.schedule(23L) {
      ChatUtils.sendSystemMessage("should reply", MessageType.DEBUG)
      ChatUtils.sendPlayerMessage(getResponses().random())
      currScript.resume()
    }

    return ReactionResult.FINISHED
  }

}
