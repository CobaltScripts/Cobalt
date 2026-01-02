package org.cobalt.internal.ui.util

import org.cobalt.Cobalt.mc

inline val mouseX: Double
  get() = mc.mouse.x

inline val mouseY: Double
  get() = mc.mouse.y

fun isHoveringOver(x: Float, y: Float, width: Float, height: Float): Boolean =
    mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height
