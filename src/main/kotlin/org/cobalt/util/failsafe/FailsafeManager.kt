package org.cobalt.util.failsafe

import org.cobalt.event.EventBus
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.TickEvent
import org.cobalt.module.ModuleManager
import java.util.PriorityQueue
import org.cobalt.module.type.Failsafe

object FailsafeManager {

  private val activeFailsafes: List<Failsafe>
    get() = ModuleManager.currentScript?.failsafes.orEmpty()

  private val emergencyQueue = PriorityQueue<Failsafe>(compareByDescending { it.priority })
  private var triggeredFailsafe: Failsafe? = null

  private var queueTimer = 0
  private var queueTarget = 0

  fun initialize() {
    EventBus.register(this)
  }

  fun addToQueue(failsafe: Failsafe) {
    val script = ModuleManager.currentScript ?: return

    if (!failsafe.enabled) {
      return
    }

    if (failsafe !in script.failsafes) {
      return
    }

    if (failsafe in emergencyQueue) {
      return
    }

    emergencyQueue.add(failsafe)
  }

  fun isFailsafeTriggered() = triggeredFailsafe != null || emergencyQueue.isNotEmpty()
  fun hasActiveReaction() = triggeredFailsafe != null

  fun stopFailsafes() {
    triggeredFailsafe = null
    emergencyQueue.clear()
    queueTimer = 0
    queueTarget = 0

    activeFailsafes.forEach {
      it.resetStates()
    }

    ModuleManager.resumeScript()
  }

  @SubscribeEvent
  fun onTick(ignored: TickEvent.Start) {
    if (ModuleManager.currentScript == null) {
      return
    }

    if (triggeredFailsafe == null) {
      processQueue()
      return
    }

    val result = triggeredFailsafe?.performReaction()

    if (result == Failsafe.ReactionResult.FINISHED) {
      stopFailsafes()
    }
  }

  private fun processQueue() {
    val currScript = ModuleManager.currentScript ?: return

    if (triggeredFailsafe != null) {
      return
    }

    if (emergencyQueue.isEmpty()) {
      queueTimer = 0
      return
    }

    if (queueTimer == 0) {
      queueTarget = currScript.failsafeDelayTicks()
    }

    if (queueTarget == 0) {
      triggeredFailsafe = emergencyQueue.poll()
      queueTimer = 0
      ModuleManager.pauseScript()
      return
    }

    if (queueTimer < queueTarget) {
      queueTimer++
      return
    }

    triggeredFailsafe = emergencyQueue.poll()
    queueTimer = 0
    ModuleManager.pauseScript()
  }

}
