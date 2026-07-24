package org.cobalt.event.annotation

import org.cobalt.event.Event

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SubscribeEvent(
  val ignoreCancelled: Boolean = false,
  val priority: Event.Priority = Event.Priority.MEDIUM,
  val once: Boolean = false,
)
