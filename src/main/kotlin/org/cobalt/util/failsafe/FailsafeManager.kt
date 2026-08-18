package org.cobalt.util.failsafe

import java.util.concurrent.ConcurrentHashMap
import org.cobalt.event.EventBus
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.TickEvent
import org.cobalt.module.ModuleManager
import org.cobalt.Cobalt
import org.cobalt.module.impl.failsafes.ChatMentionFailsafe
import org.cobalt.module.impl.failsafes.PlayerCheckFailsafe
import org.cobalt.module.impl.failsafes.RotationFailsafe
import org.cobalt.module.impl.failsafes.SlotChangeFailsafe
import org.cobalt.module.impl.failsafes.TeleportFailsafe
import org.cobalt.module.impl.failsafes.VelocityFailsafe
import org.cobalt.module.type.Failsafe
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.chat.MessageType
import org.cobalt.util.input.Mouse
import org.cobalt.util.input.MouseMode
import org.cobalt.util.scheduling.TickScheduler
import org.lwjgl.glfw.GLFW

object FailsafeManager {

  var failsafes = mutableListOf<Failsafe>()

  private val tempIgnored = ConcurrentHashMap.newKeySet<Failsafe>()
  private val ignoreGens = mutableMapOf<Failsafe, Long>()
  private var triggeredFailsafe: Failsafe? = null

  private var queueTimer = 0
  private var queueTarget = 0

  private fun registerDefaultFailsafe() {
    val builtInFailsafe = listOf<Failsafe>(
      TeleportFailsafe,
      SlotChangeFailsafe,
      VelocityFailsafe,
      ChatMentionFailsafe,
      RotationFailsafe,
      PlayerCheckFailsafe,
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

  fun ignoreFailsafe(failsafe: Failsafe) {
    val gen = (ignoreGens[failsafe] ?: 0L) + 1L
    ignoreGens[failsafe] = gen

    tempIgnored.add(failsafe) // I'm not entirely sure if this is the best method if doing this
    // change if there's a better one
    // this will just prevent the user from being alerted for 10 ticks,
    // should be long enough?, nothing else
    TickScheduler.schedule(15L, Runnable {
      if (ignoreGens[failsafe] == gen) {
        tempIgnored.remove(failsafe)
        ignoreGens.remove(failsafe)
      }
    })
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
      ChatUtils.sendSystemMessage("ERROR GRABBING WINDOW! ${e.message}", MessageType.FAILSAFE)
    }
  }


  fun alertUser(fsType: Failsafe, extraInfo: String? = null) {
    if (fsType in tempIgnored) return
    ChatUtils.sendSystemMessage("<red>POTENTIAL STAFF CHECK</red> <grey>(${fsType.name})</grey>", MessageType.FAILSAFE)
    Mouse.mouseMode = MouseMode.DEFAULT
    grabWindow()
    ChatUtils.sendSystemMessage("<red><b>DO NOT LEAVE THE GAME</b></red>", MessageType.FAILSAFE)
    if (extraInfo == null) return
    ChatUtils.sendSystemMessage(extraInfo, MessageType.FAILSAFE)

  }

}
