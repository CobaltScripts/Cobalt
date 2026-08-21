package org.cobalt.command.impl

import java.io.File
import org.cobalt.Cobalt.minecraft
import org.cobalt.command.Command
import org.cobalt.command.annotation.DefaultHandler
import org.cobalt.command.annotation.SubCommand
import org.cobalt.ui.screen.ConfigScreen
import org.cobalt.ui.screen.HudEditorScreen
import org.cobalt.event.EventBus
import org.cobalt.module.impl.failsafes.TeleportFailsafe
import org.cobalt.module.impl.misc.Rotations
import org.cobalt.util.audio.AudioManager
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.failsafe.FailsafeManager
import org.cobalt.util.rotation.data.Rotation
import org.cobalt.util.scheduling.TickScheduler
import org.cobalt.util.server.Scoreboard

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

  @SubCommand
  fun debug() {
    EventBus.getRegisteredListeners().forEach { ChatUtils.sendSystemMessage(it) }
    ChatUtils.sendSystemMessage("Is In Skyblock: ${Scoreboard.isInSkyblock()}")
  }

  @SubCommand
  fun rotate(yaw: Int, pitch: Int) {
    ChatUtils.sendSystemMessage("rotating to yaw:$yaw, pitch:$pitch")
    Rotations.start(Rotation(yaw.toFloat(), pitch.toFloat()))
    ChatUtils.sendSystemMessage("rot called")
  }
}
