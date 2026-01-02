package org.cobalt.internal.ui.notification

import java.awt.Color
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.ui.UIComponent
import org.cobalt.internal.ui.animation.SlideAnimation

internal class UINotification(
  private val title: String,
  private val description: String,
  private val duration: Long = 5000L,
) : UIComponent(
  x = 0F,
  y = 0F,
  width = 350F,
  height = 100F
) {

  private var createdAt = System.currentTimeMillis()
  private val slideInAnim = SlideAnimation(150L)
  private val slideOutAnim = SlideAnimation(150L)
  private var isClosing = false
  private val notificationWidth = 350F
  private val padding = 15F
  private val descriptionMaxWidth = notificationWidth - (padding * 2)

  private val wrappedDescription: List<String> by lazy {
    wrapText(description, descriptionMaxWidth, 12F)
  }

  fun getNotificationHeight(): Float {
    val descriptionHeight = wrappedDescription.size * 16F
    return padding + 22F + descriptionHeight + padding
  }

  init {
    slideInAnim.start()
  }

  private fun wrapText(text: String, maxWidth: Float, fontSize: Float): List<String> {
    val lines = mutableListOf<String>()
    val words = text.split(" ")
    var currentLine = ""

    for (word in words) {
      val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
      val estimatedWidth = NVGRenderer.textWidth(testLine, fontSize)

      if (estimatedWidth <= maxWidth) {
        currentLine = testLine
      } else {
        if (currentLine.isNotEmpty()) {
          lines.add(currentLine)
        }
        currentLine = word
      }
    }

    if (currentLine.isNotEmpty()) {
      lines.add(currentLine)
    }

    return lines
  }

  fun getOffsetX(): Float {
    return if (isClosing) {
      slideOutAnim.get(0F, notificationWidth)
    } else {
      notificationWidth - slideInAnim.get(0F, notificationWidth)
    }
  }

  fun shouldRemove(): Boolean {
    val elapsed = System.currentTimeMillis() - createdAt
    return elapsed > duration + 150L && isClosing && !slideOutAnim.isAnimating()
  }

  fun startClosing() {
    if (!isClosing) {
      isClosing = true
      slideOutAnim.start()
    }
  }

  override fun render() {
    val offsetX = getOffsetX()
    val finalX = x + offsetX
    val finalHeight = getNotificationHeight()

    NVGRenderer.rect(
      finalX,
      y,
      notificationWidth,
      finalHeight,
      Color(25, 25, 25).rgb,
      8F
    )

    NVGRenderer.hollowRect(
      finalX,
      y,
      notificationWidth,
      finalHeight,
      1.5F,
      Color(61, 94, 149).rgb,
      8F
    )

    NVGRenderer.text(
      title,
      finalX + padding,
      y + padding + 8F,
      14F,
      Color(230, 230, 230).rgb
    )

    var yOffset = padding + 22F
    for (line in wrappedDescription) {
      NVGRenderer.text(
        line,
        finalX + padding,
        y + yOffset,
        12F,
        Color(179, 179, 179).rgb
      )
      yOffset += 16F
    }
  }

  fun getCreatedAt(): Long = createdAt

  fun getDuration(): Long = duration
}
