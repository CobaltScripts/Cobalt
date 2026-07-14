@file:JvmName("ColorUtils")

package org.cobalt.util.color

import java.awt.Color

inline val Int.red
  get() = this shr 16 and 0xFF

inline val Int.green
  get() = this shr 8 and 0xFF

inline val Int.blue
  get() = this and 0xFF

inline val Int.alpha
  get() = this ushr 24

fun Color.updateAlpha(alpha: Int): Color {
  return Color(red, green, blue, alpha)
}
