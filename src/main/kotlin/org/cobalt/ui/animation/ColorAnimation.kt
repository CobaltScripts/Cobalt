package org.cobalt.ui.animation

import java.awt.Color
import kotlin.math.roundToInt

class ColorAnimation(duration: Long) {

  private val animation = EaseOutAnimation(duration)

  fun start() =
    animation.start()

  fun get(start: Color, end: Color, reverse: Boolean): Color =
    Color(
      animation.get(start.red.toFloat(), end.red.toFloat(), reverse).roundToInt().coerceIn(0, 255) / 255,
      animation.get(start.green.toFloat(), end.green.toFloat(), reverse).roundToInt().coerceIn(0, 255) / 255,
      animation.get(start.blue.toFloat(), end.blue.toFloat(), reverse).roundToInt().coerceIn(0, 255) / 255,
      animation.get(start.alpha.toFloat(), end.alpha.toFloat(), reverse).roundToInt().coerceIn(0, 255) / 255,
    )

  fun get(start: Int, end: Int, reverse: Boolean): Int {
    val startColor = Color(start, true)
    val endColor = Color(end, true)

    val red = animation.get(
      startColor.red.toFloat(), endColor.red.toFloat(), reverse
    ).roundToInt().coerceIn(0, 255)

    val green = animation.get(
      startColor.green.toFloat(), endColor.green.toFloat(), reverse
    ).roundToInt().coerceIn(0, 255)

    val blue = animation.get(
      startColor.blue.toFloat(), endColor.blue.toFloat(), reverse
    ).roundToInt().coerceIn(0, 255)

    val alpha = animation.get(
      startColor.alpha.toFloat(), endColor.alpha.toFloat(), reverse
    ).roundToInt().coerceIn(0, 255)

    return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
  }

}
