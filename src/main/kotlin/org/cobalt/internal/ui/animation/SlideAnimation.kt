package org.cobalt.internal.ui.animation

internal class SlideAnimation(duration: Long) : Animation<Float>(duration) {

  override fun get(start: Float, end: Float, reverse: Boolean): Float {
    val percent = getPercent() / 100f
    return start + (end - start) * percent
  }
}
