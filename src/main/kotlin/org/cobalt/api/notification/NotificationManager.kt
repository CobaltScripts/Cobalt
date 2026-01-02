package org.cobalt.api.notification

import org.cobalt.Cobalt.mc
import org.cobalt.api.event.annotation.SubscribeEvent
import org.cobalt.api.event.impl.render.NvgEvent
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.ui.notification.UINotification

object NotificationManager : NotificationAPI {

  private val notifications = mutableListOf<UINotification>()
  private val maxNotifications = 5
  private val gap = 10F

  override fun addNotification(title: String, description: String, duration: Long) {
    if (notifications.size >= maxNotifications) {
      notifications.removeAt(0)
    }
    notifications.add(UINotification(title, description, duration))
  }

  @Suppress("unused")
  @SubscribeEvent
  fun onRender(event: NvgEvent) {

    val window = mc.window
    val screenWidth = window.width.toFloat()
    val screenHeight = window.height.toFloat()

    try {
      NVGRenderer.beginFrame(screenWidth, screenHeight)

      val toRemove = mutableListOf<UINotification>()

      notifications.forEachIndexed { index, notification ->
        if (notification.shouldRemove()) {
          toRemove.add(notification)
        } else {
          val elapsed = System.currentTimeMillis() - notification.getCreatedAt()
          if (elapsed > notification.getDuration()) {
            notification.startClosing()
          }
          val yOffset = index * (notification.getNotificationHeight() + gap)
          val x = screenWidth - 350F - 15F
          val y = screenHeight - notification.getNotificationHeight() - 15F - yOffset
          notification.x = x
          notification.y = y
          notification.render()
        }
      }

      notifications.removeAll(toRemove)
      NVGRenderer.endFrame()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  override fun clear() {
    notifications.forEach { it.startClosing() }
  }
}
