package org.cobalt.command.impl

import kotlin.time.Duration.Companion.milliseconds
import org.cobalt.Cobalt.minecraft
import org.cobalt.command.Command
import org.cobalt.command.annotation.DefaultHandler
import org.cobalt.command.annotation.SubCommand
import org.cobalt.pathfinder.PathConfig
import org.cobalt.pathfinder.PathExecutor
import org.cobalt.pathfinder.calculate.PathMode
import org.cobalt.pathfinder.goal.GoalBlock
import org.cobalt.ui.notification.NotificationManager
import org.cobalt.ui.screen.ConfigScreen
import org.cobalt.ui.screen.HudEditorScreen
import org.cobalt.util.helper.Multithreading
import org.cobalt.util.helper.TickScheduler

object MainCommand : Command(name = "cobalt", aliases = listOf("cb")) {

  private const val DELAY_TICKS = 1L

  @DefaultHandler
  fun main() {
    TickScheduler.schedule(DELAY_TICKS) {
      minecraft.gui.setScreen(ConfigScreen)
    }
  }

  @SubCommand
  fun hud() {
    TickScheduler.schedule(DELAY_TICKS) {
      minecraft.gui.setScreen(HudEditorScreen)
    }
  }

  @SubCommand
  fun testNotif() {
    Multithreading.runAsync {

      NotificationManager.queue("Hi", "", 2000.milliseconds)
      Thread.sleep(750)
      NotificationManager.queue("Hi", "Hello", 2000.milliseconds)
      Thread.sleep(750)
      NotificationManager.queue(
        "Hi",
        "HeASDASDHOUASHDOUASHDUOAHSDOUAHSDOUHASOUDHASDlloHeASDASDHOUASHDOUASHDUOAHSDOUAHSDOUHASOUDHASDlloHeASDASDHOUASHDOUASHDUOAHSDOUAHSDOUHASOUDHASDlloHeASDASDHOUASHDOUASHDUOAHSDOUAHSDOUHASOUDHASDlloHeASDASDHOUASHDOUASHDUOAHSDOUAHSDOUHASOUDHASDllo",
        2000.milliseconds
      )
    }
  }

  @SubCommand
  fun goTo(x: Int, y: Int, z: Int, fly: Boolean) {
    val mode = if (fly) PathMode.FLY else PathMode.WALK
    val config = PathConfig(
      goal = GoalBlock(x, y, z),
      movements = mode.movements
    )

    PathExecutor.goTo(config)
  }

}
