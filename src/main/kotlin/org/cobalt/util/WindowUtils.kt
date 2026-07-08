package org.cobalt.util

import org.cobalt.Cobalt.minecraft

object WindowUtils {

  @JvmStatic
  val windowWidth: Float
    get() = minecraft.window.width.toFloat()

  @JvmStatic
  val windowHeight: Float
    get() = minecraft.window.height.toFloat()

  @JvmStatic
  val scaleX: Float
    get() = windowWidth / 1920f

  @JvmStatic
  val scaleY: Float
    get() = windowHeight / 1080f

}
