package org.cobalt.api.util

import java.util.*
import org.cobalt.api.event.EventBus
import org.cobalt.api.event.annotation.SubscribeEvent
import org.cobalt.api.event.impl.client.TickEvent

object TickScheduler {

  private val taskQueue = PriorityQueue<ScheduledTask>(Comparator.comparingLong(ScheduledTask::executeTick))
  private var currentTick: Long = 0

  private data class ScheduledTask(val executeTick: Long, val action: Runnable)

  init {
    EventBus.register(this)
  }

  /**
   * Schedules a task to run after a specified number of ticks.
   *
   * @param delayTicks How many ticks to wait before executing the task.
   * @param action The runnable code that should execute once the delay has passed.
   */
  fun schedule(delayTicks: Long, action: Runnable) {
    taskQueue.offer(ScheduledTask(currentTick + delayTicks, action))
  }

  @Suppress("UNUSED")
  @SubscribeEvent
  fun onClientTick(event: TickEvent.End) {
    currentTick++
    var task: ScheduledTask?

    while (taskQueue.peek().also { task = it } != null && currentTick >= task!!.executeTick) {
      taskQueue.poll().action.run()
    }
  }

}
