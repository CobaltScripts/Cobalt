package org.cobalt.api.event

import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import org.cobalt.api.event.annotation.SubscribeEvent
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder

object EventBus {

  private val listeners = ConcurrentHashMap<Class<*>, MutableList<ListenerData>>()
  private val registered = mutableSetOf<Any>()
  private val dynamicRunnables = ConcurrentHashMap<Class<out Event>, MutableList<Runnable>>()

  fun register(obj: Any) {
    if (obj in registered) return

    obj::class.java.declaredMethods.forEach { method ->
      if (method.isAnnotationPresent(SubscribeEvent::class.java)) {
        val params = method.parameterTypes
        require(params.size == 1 && Event::class.java.isAssignableFrom(params[0])) {
          "Invalid Method"
        }

        method.isAccessible = true
        val priority = method.getAnnotation(SubscribeEvent::class.java).priority

        listeners.computeIfAbsent(params[0]) { mutableListOf() }
          .add(ListenerData(obj, method, priority))

        listeners[params[0]]?.sortByDescending { it.priority }
      }
    }

    registered.add(obj)
  }

  @Suppress("UNUSED")
  fun unregister(obj: Any) {
    if (obj !in registered) return
    listeners.values.forEach { it.removeIf { data -> data.instance === obj } }
    registered.remove(obj)
  }

  fun post(event: Event): Event {
    val eventClass = event::class.java
    val applicable = listeners.flatMap { (type, methods) ->
      if (type.isAssignableFrom(eventClass)) methods else emptyList()
    }.sortedByDescending { it.priority }

    applicable.forEach { data ->
      try {
        data.method.invoke(data.instance, event)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

    handleDynamic(event)
    return event
  }

  /**
   * Registers all functions with the @SubscribeEvent annotation in the given package.
   *
   * @param packageStr The package to scan for @SubscribeEvent annotated functions.
   * @param excludeFiles A set of classes to exclude from registration.
   *
   * @author oblongboot
   */
  fun discoverAndRegister(packageStr: String, excludeFiles: Set<Class<*>> = emptySet()) {
    val reflections = Reflections(
      ConfigurationBuilder()
        .forPackages(packageStr)
        .setScanners(Scanners.MethodsAnnotated)
    )

    val methods = reflections.getMethodsAnnotatedWith(SubscribeEvent::class.java)
    val seen = mutableSetOf<Class<*>>()

    for (method in methods) {
      val clazz = method.declaringClass
      if (!seen.add(clazz) || clazz in excludeFiles) continue

      try {
        val instance = when {
          clazz.declaredFields.any { it.name == "INSTANCE" } -> {
            clazz.getDeclaredField("INSTANCE").apply { trySetAccessible() }.get(null)
          }

          else -> {
            val constructor = clazz.getDeclaredConstructor()
            constructor.trySetAccessible()
            constructor.newInstance()
          }
        }

        register(instance)
      } catch (_: Exception) { }
    }
  }

  private data class ListenerData(val instance: Any, val method: Method, val priority: Int)

  /**
   * Registers a function to be called when an event is posted, alternative to using the @SubscribeEvent annotation.
   *
   * @param eventClass The event to listen for.
   * @param runnable The function to call when the event is posted.
   *
   * @author oblongboot
   */
  fun registerEvent(eventClass: Class<out Event>, runnable: Runnable) {
      dynamicRunnables.computeIfAbsent(eventClass) { mutableListOf() }.add(runnable)
  }

  fun handleDynamic(event: Event) {
      dynamicRunnables
          .filter { (clazz, _) -> clazz.isAssignableFrom(event::class.java) }
          .forEach { (_, listeners) -> listeners.forEach { it.run() } }
  }

}
