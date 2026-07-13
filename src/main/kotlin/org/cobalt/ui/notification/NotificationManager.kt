package org.cobalt.ui.notification

import kotlin.time.Duration
import org.cobalt.event.EventBus
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.RenderEvent
import org.cobalt.util.client.WindowUtils.windowHeight
import org.cobalt.util.render.skia.SkiaPIP

object NotificationManager {

  private val notifQueue = mutableListOf<Notification>()
  private val activeNotifications = mutableListOf<Notification>()

  init {
    EventBus.register(this)
  }

  fun queue(title: String, description: String, duration: Duration) {
    val notification = Notification(
      title,
      description,
      duration
    )

    notifQueue.add(notification)
  }

  fun clear() {
    notifQueue.clear()
    activeNotifications.clear()
  }

  @SubscribeEvent
  fun onNotificationRender(event: RenderEvent.Notification) {
    val currentTime = System.currentTimeMillis()

    activeNotifications.forEach { it.checkExpiry(currentTime) }
    activeNotifications.removeIf { it.isDone() }

    while (activeNotifications.size < 3 && notifQueue.isNotEmpty()) {
      val notif = notifQueue.removeAt(0)
      val targetY = computeTargetY(windowHeight, activeNotifications.size, notif.height)

      notif.targetY = targetY
      notif.previousY = targetY
      notif.start(currentTime)

      activeNotifications.add(notif)
    }

    activeNotifications.forEachIndexed { index, notification ->
      notification.moveTo(computeTargetY(windowHeight, index, notification.height))
    }

    SkiaPIP.drawSkia(event.graphics) {
      activeNotifications.forEach { notification ->
        notification.renderComponent()
      }
    }
  }

  private fun computeTargetY(screenHeight: Float, index: Int, notifHeight: Float): Float {
    return screenHeight - (index + 1) * (notifHeight + 10f) - 10f
  }

}
