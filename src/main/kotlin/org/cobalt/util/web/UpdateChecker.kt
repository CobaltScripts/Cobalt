package org.cobalt.util.web

import com.google.gson.JsonParser
import java.net.URI
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import org.cobalt.Cobalt
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.chat.MessageType
import org.slf4j.LoggerFactory

object UpdateChecker {

  private var shouldAnnounce = true
  private var newVersion: String = "${Cobalt.MINECRAFT_VERSION}-${Cobalt.MOD_VERSION}"
  private val logger = LoggerFactory.getLogger(this::class.java)

  fun runCheck() {
    if (FabricLoader.getInstance().isDevelopmentEnvironment) {
      return
    }

    try {
      val result = ResourceUtils.getInputStream(
        url = "https://api.github.com/repos/CobaltScripts/Cobalt/releases/latest",
        timeout = 1000,
        cache = false
      ).use {
        it.reader().readText()
      }

      val json = JsonParser.parseString(result)

      newVersion = json.asJsonObject.get("tag_name").asString
        .replace(Regex("""\.[0-9a-f]{7,40}$"""), "")
        .replace(Cobalt.MINECRAFT_VERSION + "-", "")

      if (compareVersions(newVersion) < 0) {
        shouldAnnounce = true
        registerAnnouncer()
      }
    } catch (_: Exception) {
      shouldAnnounce = false
      logger.error("Update Checker request failed, internet connection or github down?")
    }
  }

  private fun registerAnnouncer() {
    ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
      if (!shouldAnnounce) {
        return@register
      }

      shouldAnnounce = false
      ChatUtils.sendLineBreak()

      ChatUtils.sendSystemMessage(
        message = "<color:#4CADD0>An update for Cobalt is available!<reset>",
        type = MessageType.RAW
      )

      ChatUtils.sendSystemMessage(
        message = "<gray>Version:</gray> " +
          "<red>${Cobalt.MOD_VERSION}</red> <dark_gray>→</dark_gray> <green>${newVersion}</green>",
        type = MessageType.RAW
      )

      val updateComponent = Component.empty().apply {
        append(
          createLink(
            label = "[DOWNLOAD]",
            color = ChatFormatting.GREEN,
            url = "https://github.com/CobaltScripts/Cobalt/releases/latest"
          )
        )
        append(" ")
        append(createLink(
          label = "[DISCORD]",
          color = ChatFormatting.BLUE,
          url = "https://cobalt.quiteboring.dev/discord/")
        )
        append(" ")
        append(createLink(
          label = "[WEBSITE]",
          color = ChatFormatting.GOLD,
          url = "https://cobalt.quiteboring.dev/")
        )
      }

      ChatUtils.sendSystemMessage(updateComponent, MessageType.RAW)
      ChatUtils.sendLineBreak()
    }
  }

  private fun createLink(label: String, color: ChatFormatting, url: String): Component {
    return Component.literal(label).apply {
      style = style
        .withColor(color)
        .withBold(true)
        .withClickEvent(ClickEvent.OpenUrl(URI.create(url)))
    }
  }

  private fun compareVersions(remoteVersion: String): Int {
    val localParts = Cobalt.MOD_VERSION.split(".").map { it.toIntOrNull() ?: 0 }
    val remoteParts = remoteVersion.split(".").map { it.toIntOrNull() ?: 0 }

    for (i in 0 until maxOf(localParts.size, remoteParts.size)) {
      val localPart = localParts.getOrElse(i) { 0 }
      val remotePart = remoteParts.getOrElse(i) { 0 }

      if (localPart != remotePart) {
        return localPart.compareTo(remotePart)
      }
    }

    return 0
  }

}
