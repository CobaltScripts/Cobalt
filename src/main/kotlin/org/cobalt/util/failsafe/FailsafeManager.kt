package org.cobalt.util.failsafe

import org.cobalt.event.EventBus
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.TickEvent
import org.cobalt.module.ModuleManager
import org.cobalt.Cobalt
import org.cobalt.module.impl.failsafes.TeleportFailsafe
import org.cobalt.module.type.Failsafe
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.chat.MessageType
import org.cobalt.util.input.Mouse
import org.cobalt.util.input.MouseMode
import org.lwjgl.glfw.GLFW

object FailsafeManager {

  var failsafes = mutableListOf<Failsafe>()
  private var triggeredFailsafe: Failsafe? = null

  private var queueTimer = 0
  private var queueTarget = 0

  private fun registerDefaultFailsafe() {
    val builtInFailsafe = listOf<Failsafe>(
      TeleportFailsafe
    )

    builtInFailsafe.forEach { registerFailsafe(it) }
  }

  fun registerFailsafe(failsafe: Failsafe) {
    failsafes.add(failsafe)
    failsafe.loadConfig()
    failsafe.onRegistration()
    EventBus.register(failsafe)
  }

  fun initialize() {
    registerDefaultFailsafe()
    EventBus.register(this)
  }


  fun isFailsafeTriggered() = triggeredFailsafe != null
  fun hasActiveReaction() = triggeredFailsafe != null

  fun stopFailsafes() {
    triggeredFailsafe = null
    queueTimer = 0
    queueTarget = 0

    failsafes.forEach {
      it.resetStates()
    }

    ModuleManager.resumeScript()
  }

  @SubscribeEvent
  fun onTick(ignored: TickEvent.Start) {
    if (ModuleManager.currentScript == null) {
      return
    }

    val result = triggeredFailsafe?.performReaction()

    if (result == Failsafe.ReactionResult.FINISHED) {
      stopFailsafes()
    }
  }

  fun grabWindow() {
    try {
      GLFW.glfwFocusWindow(Cobalt.minecraft.window.handle())
    } catch (e: Exception) {
      ChatUtils.sendSystemMessage("FAILED TO GRAB WINDOW, PLEASE REPORT THIS! ERROR: ${e.message}", MessageType.FAILSAFE)
    }
  }


  fun alertUser(fsType: Failsafe) {
    ChatUtils.sendSystemMessage("POTENTIAL STAFF CHECK (${fsType.name})", MessageType.FAILSAFE)
    Mouse.mouseMode = MouseMode.DEFAULT
    grabWindow()
    ChatUtils.sendSystemMessage("DO NOT LEAVE THE GAME", MessageType.FAILSAFE)

  }

}
