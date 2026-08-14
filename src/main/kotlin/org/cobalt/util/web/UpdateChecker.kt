package org.cobalt.util.web

import com.google.gson.JsonParser
import java.net.URI
import java.net.URL
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.SharedConstants
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import org.cobalt.Cobalt
import org.cobalt.util.chat.ChatFormatter
import org.cobalt.util.chat.ChatUtils
import org.slf4j.LoggerFactory

object UpdateChecker {
  private var shouldAnnounce = false
  private var newVersion: String = "${Cobalt.MINECRAFT_VERSION}-${Cobalt.MOD_VERSION}"
  private val logger = LoggerFactory.getLogger(this::class.java)

  fun runCheck() {
    if (FabricLoader.getInstance().isDevelopmentEnvironment) return

    try {
      val inputStream =
        ResourceUtils.getInputStream("https://api.github.com/repos/CobaltScripts/Cobalt/releases/latest", 1000, false)

      val result = inputStream.reader().readText()
      val json = JsonParser.parseString(result)
      newVersion =
        json.asJsonObject.get("tag_name").asString
          .replace(Regex("""\.[0-9a-f]{7,40}$"""), "")
          .replace(Cobalt.MINECRAFT_VERSION + "-", "")

      when {
        compareVersions( newVersion) < 0 -> {
          shouldAnnounce = true
          registerAnnouncer()
        }
        compareVersions(newVersion) == 0 -> return
        compareVersions(newVersion) > 0 -> return
      }
    } catch (exception: Exception) {
      shouldAnnounce = false
      logger.error("Update Checker request failed, internet connection or github down?")
      exception.printStackTrace()
    }
  }

  private fun registerAnnouncer() {
    ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
      if (!shouldAnnounce) return@register
      shouldAnnounce = false
      ChatUtils.sendLineBreak()
      ChatUtils.sendSystemMessage("<grey>Update Available!</grey>")
      ChatUtils.sendSystemMessage(
        "<grey>From version</grey> <red>${Cobalt.MOD_VERSION}</red> <gray>-></gray> <green>${newVersion}</green>"
      )
      ChatUtils.sendSystemMessage(
        ChatFormatter.parse("<gradient:#4CADD0:#B2F9FF>Download </gradient>")
          .append(
            Component.literal("[HERE]")
              .withColor(TextColor.AQUA)
              .withStyle(
                Style.EMPTY.withUnderlined(true)
                  .withClickEvent(
                    ClickEvent.OpenUrl(
                      URI.create("https://github.com/CobaltScripts/Cobalt/releases/latest")
                    )
                  )
              )
          )
      )
      ChatUtils.sendLineBreak()
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
