package org.cobalt.api.hud.modules.bestiary

import org.cobalt.api.event.EventBus
import org.cobalt.api.event.annotation.SubscribeEvent
import org.cobalt.api.event.impl.client.TickEvent
import org.cobalt.api.hud.HudAnchor
import org.cobalt.api.hud.hudElement
import org.cobalt.api.module.Module
import org.cobalt.api.module.setting.impl.CheckboxSetting
import org.cobalt.api.module.setting.impl.SliderSetting
import org.cobalt.api.module.setting.impl.TextSetting
import org.cobalt.api.ui.theme.ThemeManager
import org.cobalt.api.util.ui.NVGRenderer

class BestiaryModule : Module("Bestiary Tracker") {

  private val textSize = 13f
  private val headerSize = 15f
  private val lineSpacing = 16f
  private val padding = 8f
  private val maxDisplayMobs = 10

  private var tickCounter = 0

  val bestiaryHud = hudElement("bestiary-tracker", "Bestiary Tracker", "Tracks Hypixel SkyBlock Bestiary progress") {
    anchor = HudAnchor.TOP_RIGHT
    offsetX = 10f
    offsetY = 10f

    val apiKeySetting = setting(TextSetting("API Key", "Your Hypixel API key", ""))
    val uuidSetting = setting(TextSetting("UUID", "Your Minecraft UUID (no dashes)", ""))
    val refreshInterval = setting(SliderSetting("Refresh (s)", "API refresh interval in seconds", 60.0, 10.0, 300.0))
    val showPerHour = setting(CheckboxSetting("Kills/Hour", "Show kills per hour", true))
    val showSession = setting(CheckboxSetting("Session Kills", "Show session kill count", true))
    val showProgress = setting(CheckboxSetting("Tier Progress", "Show progress to next tier", true))
    val showTopMobs = setting(CheckboxSetting("Top Mobs", "Show top mob breakdown", true))
    val background = setting(CheckboxSetting("Background", "Show background panel", true))

    width {
      val w = 220f
      w
    }

    height {
      var lines = 3f // header + total kills + session duration
      if (showSession.value) lines += 1f
      if (showPerHour.value) lines += 1f
      if (showTopMobs.value) {
        lines += 1f // "Top Mobs" header
        val mobCount = BestiaryTracker.currentMobs.take(maxDisplayMobs).size.coerceAtLeast(1)
        lines += mobCount * (if (showProgress.value) 2f else 1f)
      }
      if (BestiaryTracker.lastError != null) lines += 1f
      lines * lineSpacing + padding * 2
    }

    render { screenX, screenY, _ ->
      BestiaryTracker.apiKey = apiKeySetting.value
      BestiaryTracker.playerUuid = uuidSetting.value
      BestiaryTracker.refreshIntervalSeconds = refreshInterval.value

      val w = 220f
      var y = screenY + padding
      val x = screenX + padding
      val contentWidth = w - padding * 2

      val panelColor = ThemeManager.currentTheme.panel
      val accentColor = ThemeManager.currentTheme.accent
      val textColor = -1 // white
      val dimColor = 0xFFAAAAAA.toInt()
      val greenColor = 0xFF55FF55.toInt()
      val progressBgColor = 0xFF333333.toInt()
      val progressFillColor = 0xFF4CADD0.toInt()

      // Background - compute height dynamically
      if (background.value) {
        var bgLines = 3f
        if (showSession.value) bgLines += 1f
        if (showPerHour.value) bgLines += 1f
        if (showTopMobs.value) {
          bgLines += 1f
          val mobCount = BestiaryTracker.currentMobs.take(maxDisplayMobs).size.coerceAtLeast(1)
          bgLines += mobCount * (if (showProgress.value) 2f else 1f)
        }
        if (BestiaryTracker.lastError != null) bgLines += 1f
        val bgHeight = bgLines * lineSpacing + padding * 2
        NVGRenderer.rect(screenX, screenY, w, bgHeight, panelColor, 6f)
      }

      // Header
      NVGRenderer.text("Bestiary Tracker", x, y, headerSize, accentColor)
      y += lineSpacing + 2f

      // Error state
      if (BestiaryTracker.lastError != null) {
        NVGRenderer.text("Error: ${BestiaryTracker.lastError}", x, y, textSize - 1f, 0xFFFF5555.toInt())
        y += lineSpacing
      }

      if (BestiaryTracker.currentMobs.isEmpty() && BestiaryTracker.lastError == null) {
        if (apiKeySetting.value.isBlank() || uuidSetting.value.isBlank()) {
          NVGRenderer.text("Set API Key & UUID in settings", x, y, textSize, dimColor)
        } else {
          NVGRenderer.text("Fetching data...", x, y, textSize, dimColor)
        }
        return@render
      }

      // Total kills
      NVGRenderer.text("Total Kills:", x, y, textSize, dimColor)
      val totalText = BestiaryTracker.formatNumber(BestiaryTracker.totalKills)
      NVGRenderer.text(totalText, x + contentWidth - NVGRenderer.textWidth(totalText, textSize), y, textSize, textColor)
      y += lineSpacing

      // Session duration
      NVGRenderer.text("Session:", x, y, textSize, dimColor)
      val durationText = BestiaryTracker.getSessionDurationFormatted()
      NVGRenderer.text(durationText, x + contentWidth - NVGRenderer.textWidth(durationText, textSize), y, textSize, textColor)
      y += lineSpacing

      // Session kills
      if (showSession.value) {
        NVGRenderer.text("Session Kills:", x, y, textSize, dimColor)
        val sessionText = BestiaryTracker.formatNumber(BestiaryTracker.getSessionKills())
        NVGRenderer.text(sessionText, x + contentWidth - NVGRenderer.textWidth(sessionText, textSize), y, textSize, greenColor)
        y += lineSpacing
      }

      // Kills per hour
      if (showPerHour.value) {
        NVGRenderer.text("Kills/Hour:", x, y, textSize, dimColor)
        val perHourText = BestiaryTracker.formatNumber(BestiaryTracker.getKillsPerHour())
        NVGRenderer.text(perHourText, x + contentWidth - NVGRenderer.textWidth(perHourText, textSize), y, textSize, textColor)
        y += lineSpacing
      }

      // Top mobs breakdown
      if (showTopMobs.value) {
        y += 4f
        NVGRenderer.text("Top Mobs", x, y, textSize, accentColor)
        y += lineSpacing

        val topMobs = BestiaryTracker.currentMobs.take(maxDisplayMobs)
        for (mob in topMobs) {
          // Mob name and kills
          val mobName = if (mob.name.length > 16) mob.name.take(15) + ".." else mob.name
          NVGRenderer.text(mobName, x, y, textSize - 1f, textColor)

          val killsText = "${BestiaryTracker.formatNumber(mob.kills)} (T${mob.tier})"
          NVGRenderer.text(killsText, x + contentWidth - NVGRenderer.textWidth(killsText, textSize - 1f), y, textSize - 1f, dimColor)
          y += lineSpacing

          // Progress bar to next tier
          if (showProgress.value) {
            if (mob.tier < mob.maxTier) {
              val killsIntoTier = mob.kills - mob.killsForCurrentTier
              val killsNeeded = mob.killsForNextTier - mob.killsForCurrentTier
              val progress = if (killsNeeded > 0) (killsIntoTier.toFloat() / killsNeeded).coerceIn(0f, 1f) else 1f

              val barWidth = contentWidth - 4f
              val barHeight = 6f
              val barX = x + 2f

              // Background bar
              NVGRenderer.rect(barX, y, barWidth, barHeight, progressBgColor, 3f)
              // Fill bar
              if (progress > 0f) {
                NVGRenderer.rect(barX, y, barWidth * progress, barHeight, progressFillColor, 3f)
              }
              // Progress text
              val progressText = "${BestiaryTracker.formatNumber(killsIntoTier)}/${BestiaryTracker.formatNumber(killsNeeded)}"
              val progressPercent = "%.0f%%".format(progress * 100)
              NVGRenderer.text(progressPercent, x + contentWidth - NVGRenderer.textWidth(progressPercent, textSize - 2f), y + barHeight + 1f, textSize - 2f, dimColor)
            } else {
              NVGRenderer.text("MAX", x + 2f, y, textSize - 2f, greenColor)
            }
            y += lineSpacing
          }
        }
      }
    }
  }

  init {
    EventBus.register(this)
  }

  @SubscribeEvent
  fun onTick(event: TickEvent.End) {
    if (BestiaryTracker.apiKey.isBlank() || BestiaryTracker.playerUuid.isBlank()) return

    tickCounter++
    // 20 ticks = 1 second, multiply by refresh interval from settings
    val refreshTicks = (BestiaryTracker.refreshIntervalSeconds * 20).toInt().coerceAtLeast(200)
    if (tickCounter >= refreshTicks || BestiaryTracker.lastFetchTime == 0L) {
      tickCounter = 0
      BestiaryTracker.fetchBestiary()
    }
  }
}
