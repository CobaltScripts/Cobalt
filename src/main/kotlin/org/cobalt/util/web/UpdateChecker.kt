package org.cobalt.util.web

import com.google.gson.JsonParser
import java.net.URI
import java.net.URL
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.SharedConstants
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import org.cobalt.Cobalt
import org.cobalt.util.chat.ChatFormatter
import org.cobalt.util.chat.ChatUtils


//TODO: clean up this code, i will do it dw
object UpdateChecker {
  private var shouldAnnounce = false
  private var newVersion: String? = null

  fun runCheck() {
    val currentVersion = "${SharedConstants.getCurrentVersion().id()}-${Cobalt.MOD_VERSION}"

    val inputStream = ResourceUtils.getInputStream("https://api.github.com/repos/CobaltScripts/Cobalt/releases/latest", 1000, false)

    val result = inputStream.reader().readText()
    val json = JsonParser.parseString(result)
    newVersion = json.asJsonObject.get("tag_name").asString.replace(Regex("""\.[0-9a-f]{7,40}$"""), "")

    if (newVersion != currentVersion) {
      shouldAnnounce = true
      registerAnnouncer()
    }
  }

  private fun registerAnnouncer() {
    ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
      if (!shouldAnnounce) return@register
      shouldAnnounce = false
      ChatUtils.sendSystemMessage("<aqua>" + "-".repeat(45) + "</aqua>")
      ChatUtils.sendSystemMessage("<grey>Update Available!</grey>")
      ChatUtils.sendSystemMessage("<grey>From version</grey> <red>${Cobalt.MOD_VERSION}</red> <gray>-></gray> <green>${newVersion?.replace(SharedConstants.getCurrentVersion().id() + "-", "")}</green>")
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
      ChatUtils.sendSystemMessage("<aqua>" + "-".repeat(45) + "</aqua>")
  }
}}
