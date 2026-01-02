package org.cobalt.internal.ui.animation

import java.awt.Color

internal class ColorAnimation(duration: Long) {

  private val anim = EaseOutAnimation(duration)

  fun start() = anim.start()

  fun isAnimating(): Boolean = anim.isAnimating()

  fun percent(): Float = anim.getPercent()

  fun get(start: Color, end: Color, reverse: Boolean): Color =
    Color(
      anim.get(start.red.toFloat(), end.red.toFloat(), reverse) / 255,
      anim.get(start.green.toFloat(), end.green.toFloat(), reverse) / 255,
      anim.get(start.blue.toFloat(), end.blue.toFloat(), reverse) / 255,
      anim.get(start.alpha.toFloat(), end.alpha.toFloat(), reverse) / 255,
    )

}
