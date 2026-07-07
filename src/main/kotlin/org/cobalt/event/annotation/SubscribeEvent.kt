package org.cobalt.event.annotation

import org.cobalt.event.Event

/**
 * Marks a function to be called when the specified event is fired
 * Must be registered with [org.cobalt.event.EventBus.register] in your class/object
 * If you dislike like annotation style methods like this or prefer lambdas, use [org.cobalt.event.EventBus.registerLambda], there are docs on that too
 *
 * @property ignoreCancelled whether this function should still be called if the event has been canceled
 * @property priority the priority this runs at
 * @property once should this event be unregistered after it has been run once
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SubscribeEvent(
  val ignoreCancelled: Boolean = false,
  val priority: Event.Priority = Event.Priority.MEDIUM,
  val once: Boolean = false,
)
