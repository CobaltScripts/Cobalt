package org.cobalt.util.scheduling

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

object Multithreading {

  private val counter = AtomicInteger()
  private val pool = Executors.newFixedThreadPool(
    Runtime.getRuntime().availableProcessors()
  ) { runnable ->
    Thread(runnable, "Cobalt Worker ${counter.incrementAndGet()}").apply {
      isDaemon = true
    }
  }

  @JvmStatic
  fun runAsync(runnable: Runnable) {
    pool.execute(runnable)
  }

}
