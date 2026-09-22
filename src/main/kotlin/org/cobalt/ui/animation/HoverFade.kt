package org.cobalt.ui.animation

import java.awt.Color

/** Shared hover-transition animation: restarts a color/alpha fade whenever the hover state flips. */
class HoverFade(duration: Long = 150L) {

  private val colorAnimation = ColorAnimation(duration)
  private val alphaAnimation = EaseOutAnimation(duration)
  private var wasHovering = false

  fun update(hovering: Boolean) {
    if (hovering != wasHovering) {
      colorAnimation.start()
      alphaAnimation.start()
      wasHovering = hovering
    }
  }

  fun overlayAlpha(hovering: Boolean, from: Float = 0f, to: Float = 40f): Int =
    alphaAnimation.get(from, to, !hovering).toInt()

  fun color(from: Color, to: Color, hovering: Boolean): Color =
    colorAnimation.get(from, to, !hovering)

}
