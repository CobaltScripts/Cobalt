package org.cobalt.command.impl

import kotlin.time.Duration.Companion.seconds
import org.cobalt.Cobalt.minecraft
import org.cobalt.command.Command
import org.cobalt.command.annotation.DefaultHandler
import org.cobalt.command.annotation.SubCommand
import org.cobalt.ui.screen.ConfigScreen
import org.cobalt.ui.screen.HudEditorScreen
import org.cobalt.event.EventBus
import org.cobalt.ui.notification.NotificationManager
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.scheduling.TickScheduler

object MainCommand : Command(name = "cobalt", aliases = listOf("cb")) {

  @DefaultHandler
  fun main() {
    TickScheduler.schedule(1L) {
      minecraft.setScreen(ConfigScreen)
    }
  }

  @SubCommand
  fun hud() {
    TickScheduler.schedule(1L) {
      minecraft.setScreen(HudEditorScreen)
    }
  }

  @SubCommand
  fun debug() {
    NotificationManager.queue("Test", "Testing123", 2.seconds)
    EventBus.getRegisteredListeners().forEach { ChatUtils.sendSystemMessage(it) }
  }

}
