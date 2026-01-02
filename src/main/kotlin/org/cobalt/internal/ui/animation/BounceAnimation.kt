package org.cobalt.internal.ui.animation

import kotlin.math.pow

internal class BounceAnimation(duration: Long) : Animation<Float>(duration) {

  override fun get(start: Float, end: Float, reverse: Boolean): Float {
    if (!isAnimating()) return if (reverse) start else end
    return if (reverse) end + (start - end) * ease() else start + (end - start) * ease()
  }

  private fun ease(): Float {
    val x = getPercent() / 100f

    return when {
      x < 0.3f -> {
        val t = x / 0.3f
        val easeOut = 1f - (1f - t).pow(3)
        easeOut * 1.05f
      }

      else -> {
        val t = (x - 0.3f) / 0.7f
        val easeOut = 1f - (1f - t).pow(2)
        1.05f - (0.05f * easeOut)
      }
    }
  }

}
