package org.cobalt.event

import java.lang.invoke.MethodHandles
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import org.cobalt.event.EventBus.unregister
import org.cobalt.event.annotation.SubscribeEvent
import org.slf4j.LoggerFactory

object EventBus {

  private val handlers = CopyOnWriteArrayList<Handler>()
  private val dispatchCache = ConcurrentHashMap<Class<*>, Array<Handler>>()
  private val metadataCache = ConcurrentHashMap<Class<*>, List<MethodMetadata>>()
  private val logger = LoggerFactory.getLogger(this::class.java)

  @JvmStatic
  fun register(listener: Any) {
    if (handlers.any { it.listener === listener }) {
      return
    }

    val toAdd = metadataCache.computeIfAbsent(listener.javaClass, ::scanClassForHandlers)
      .map { metadata ->
        createHandler(listener, metadata)
      }

    if (toAdd.isEmpty()) {
      return
    }

    handlers.addAll(toAdd)
    dispatchCache.clear()
  }

  @JvmStatic
  inline fun <reified T : Event> registerLambda(
    priority: Event.Priority = Event.Priority.MEDIUM,
    receiveCancelled: Boolean = false,
    once: Boolean = false,
    noinline handler: (T) -> Unit,
  ): Any = registerLambdaInternal(
    eventType = T::class.java,
    priority = priority,
    receiveCancelled = receiveCancelled,
    once = once,
    handler = handler
  )

  @JvmStatic
  @PublishedApi
  internal fun <T : Event> registerLambdaInternal(
    eventType: Class<T>,
    priority: Event.Priority,
    receiveCancelled: Boolean,
    once: Boolean,
    handler: (T) -> Unit,
  ): Any {
    val token = Any()
    val h = Handler(
      listener = token,
      eventType = eventType,
      priority = priority,
      receiveCancelled = receiveCancelled,
      once = once,
      methodName = "<lambda:${eventType.simpleName}>",
      invoker = { event ->
        @Suppress("UNCHECKED_CAST")
        handler(event as T)
      },
    )
    handlers.add(h)
    dispatchCache.clear()
    return token
  }

  private fun scanClassForHandlers(listenerClass: Class<*>): List<MethodMetadata> {
    return listenerClass.declaredMethods.mapNotNull { method ->
      val annotation = method.getAnnotation(SubscribeEvent::class.java) ?: return@mapNotNull null
      val params = method.parameterTypes

      require(params.size == 1) {
        "EventBus: ${listenerClass.name}#${method.name} must take exactly one Event argument"
      }

      require(Event::class.java.isAssignableFrom(params[0])) {
        "EventBus: ${listenerClass.name}#${method.name} parameter ${params[0].name} is not an Event"
      }

      require(method.returnType == Void.TYPE) {
        "EventBus: ${listenerClass.name}#${method.name} must return Unit/void"
      }

      require(method.trySetAccessible()) {
        "EventBus: could not access ${listenerClass.name}#${method.name}"
      }

      MethodMetadata(
        method = method,
        eventType = params[0].asSubclass(Event::class.java),
        priority = annotation.priority,
        receiveCancelled = annotation.ignoreCancelled,
        once = annotation.once,
      )
    }
  }

  private fun createHandler(listener: Any, metadata: MethodMetadata): Handler {
    val handle = MethodHandles
      .privateLookupIn(listener.javaClass, MethodHandles.lookup())
      .unreflect(metadata.method)
      .bindTo(listener)

    return Handler(
      listener = listener,
      eventType = metadata.eventType,
      priority = metadata.priority,
      receiveCancelled = metadata.receiveCancelled,
      once = metadata.once,
      methodName = metadata.method.name,
      invoker = { event -> handle.invoke(event) },
    )
  }

  private fun removeHandlers(toRemove: List<Handler>) {
    if (toRemove.isEmpty()) {
      return
    }

    handlers.removeIf { handler ->
      toRemove.any { it === handler }
    }

    dispatchCache.clear()
  }

  @JvmStatic
  fun unregister(listener: Any) {
    handlers.removeIf { it.listener === listener }
    dispatchCache.clear()
  }

  @JvmStatic
  fun post(event: Event): Event {
    val matched = dispatchCache.computeIfAbsent(event.javaClass) { cls ->
      handlers
        .filter { it.eventType.isAssignableFrom(cls) }
        .sortedBy { it.priority.ordinal }
        .toTypedArray()
    }

    val toRemove = mutableListOf<Handler>()

    for (handler in matched) {
      if (event is Event.Cancellable && event.isCancelled() && !handler.receiveCancelled) {
        continue
      }

      try {
        handler.invoker(event)
      } catch (throwable: Throwable) {
        logger.error(
          "EventBus: exception in ${handler.listener.javaClass.name}#${handler.methodName} " +
            "while handling ${event.javaClass.name}",
          throwable
        )
      }

      if (handler.once) {
        toRemove.add(handler)
      }
    }

    removeHandlers(toRemove)

    return event
  }

  private data class MethodMetadata(
    val method: Method,
    val eventType: Class<out Event>,
    val priority: Event.Priority,
    val receiveCancelled: Boolean,
    val once: Boolean,
  )

  private class Handler(
    val listener: Any,
    val eventType: Class<out Event>,
    val priority: Event.Priority,
    val receiveCancelled: Boolean,
    val once: Boolean,
    val methodName: String,
    val invoker: (Event) -> Unit,
  )

}
