package org.cobalt.internal.ui.util

import kotlin.math.max

internal class ScrollHandler(
  private val scrollSpeed: Float = 20f
) {

  private var scroll = 0f
  private var maxScroll = 0f

  fun handleScroll(amount: Double) {
    scroll -= (amount * scrollSpeed).toFloat()
    scroll = scroll.coerceIn(0f, maxScroll)
  }

  fun setMaxScroll(contentHeight: Float, visibleHeight: Float) {
    maxScroll = max(0f, contentHeight - visibleHeight)
    scroll = scroll.coerceIn(0f, maxScroll)
  }

  fun getOffset(): Float = scroll
  fun isScrollable(): Boolean = maxScroll > 0f
  fun getMaxScroll(): Float = maxScroll

  fun reset() {
    scroll = 0f
  }

}
