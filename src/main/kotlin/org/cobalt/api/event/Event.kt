package org.cobalt.api.event

abstract class Event(private val cancellable: Boolean = false) {

  private var value: Boolean = false

  @Suppress("UNUSED")
  fun setCancelled(value: Boolean) {
    if (cancellable) {
      this.value = value
    }
  }

  fun isCancelled(): Boolean {
    if (!cancellable) {
      return false
    }

    return value
  }

  fun post(): Boolean {
    EventBus.post(this)
    return isCancelled()
  }

}

