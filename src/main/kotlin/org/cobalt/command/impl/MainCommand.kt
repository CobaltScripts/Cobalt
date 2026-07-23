package org.cobalt.command.impl

import org.cobalt.Cobalt.minecraft
import org.cobalt.command.Command
import org.cobalt.command.annotation.DefaultHandler
import org.cobalt.command.annotation.SubCommand
import org.cobalt.ui.screen.ConfigScreen
import org.cobalt.ui.screen.HudEditorScreen
import org.cobalt.util.scheduling.TickScheduler

object MainCommand : Command(name = "cobalt", aliases = listOf("cb")) {

  @DefaultHandler
  fun main() {
    TickScheduler.schedule(1L) {
      minecraft.gui.setScreen(ConfigScreen)
    }
  }

  @SubCommand
  fun hud() {
    TickScheduler.schedule(1L) {
      minecraft.gui.setScreen(HudEditorScreen)
    }
  }

}
