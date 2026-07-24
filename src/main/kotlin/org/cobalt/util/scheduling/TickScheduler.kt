package org.cobalt.util.scheduling

import java.util.PriorityQueue
import org.cobalt.event.EventBus
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.TickEvent

object TickScheduler {

  private var currentTick: Long = 0
  private val taskQueue = PriorityQueue<ScheduledTask>(
    Comparator.comparingLong(ScheduledTask::executeTick)
  )

  init {
    EventBus.register(this)
  }

  @JvmStatic
  fun schedule(delayTicks: Long, action: Runnable) {
    taskQueue.offer(ScheduledTask(currentTick + delayTicks, action))
  }

  @SubscribeEvent
  fun onClientTick(ignored: TickEvent.End) {
    if (taskQueue.isEmpty()) return

    currentTick++

    while (true) {
      val task = taskQueue.peek() ?: break

      if (currentTick < task.executeTick) {
        break
      }

      taskQueue.poll().action.run()
    }
  }

  private data class ScheduledTask(val executeTick: Long, val action: Runnable)

}
