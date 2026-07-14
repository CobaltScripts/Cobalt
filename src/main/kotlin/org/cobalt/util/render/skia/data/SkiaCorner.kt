package org.cobalt.util.render.skia.data

enum class SkiaCorner {

  TOP_LEFT,
  TOP_RIGHT,
  BOTTOM_LEFT,
  BOTTOM_RIGHT;

  companion object {

    @JvmStatic
    val ALL = arrayOf(TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT)

    @JvmStatic
    val LEFT_SIDE = arrayOf(TOP_LEFT, BOTTOM_LEFT)

    @JvmStatic
    val RIGHT_SIDE = arrayOf(TOP_RIGHT, BOTTOM_RIGHT)

    @JvmStatic
    val TOP_SIDE = arrayOf(TOP_LEFT, TOP_RIGHT)

    @JvmStatic
    val BOTTOM_SIDE = arrayOf(BOTTOM_LEFT, BOTTOM_RIGHT)

  }

}
