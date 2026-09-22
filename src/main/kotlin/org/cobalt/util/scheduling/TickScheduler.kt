package org.cobalt.util.scheduling

import java.util.concurrent.PriorityBlockingQueue
import org.cobalt.event.EventBus
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.TickEvent

object TickScheduler {

  private var currentTick: Long = 0

  // PriorityBlockingQueue rather than PriorityQueue: schedule() can be called from Multithreading's worker
  // threads while onClientTick concurrently drains this on the client thread.
  private val taskQueue = PriorityBlockingQueue<ScheduledTask>(
    11,
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
