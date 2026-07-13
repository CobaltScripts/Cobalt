package org.cobalt.ui.notification

import kotlin.time.Duration
import org.cobalt.event.EventBus
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.RenderEvent
import org.cobalt.util.WindowUtils.windowHeight
import org.cobalt.util.skia.SkiaPIP

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
      notif.start(currentTime)
      activeNotifications.add(notif)
    }

    var currentY = windowHeight - SCREEN_MARGIN

    for (i in activeNotifications.size - 1 downTo 0) {
      val notif = activeNotifications[i]
      currentY -= notif.height
      val targetY = currentY

      if (notif.targetY == 0f) {
        notif.targetY = targetY
        notif.previousY = targetY
      } else {
        notif.moveTo(targetY)
      }

      currentY -= SCREEN_MARGIN
    }

    SkiaPIP.drawSkia(event.graphics) {
      activeNotifications.forEach { notification ->
        notification.renderComponent()
      }
    }
  }

  private const val SCREEN_MARGIN: Float = 10f

}
