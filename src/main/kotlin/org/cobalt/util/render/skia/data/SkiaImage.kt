package org.cobalt.util.render.skia.data

import org.cobalt.util.render.skia.ResourceUtils

class SkiaImage(
  val location: String,
  val isSvg: Boolean = location.endsWith(".svg", ignoreCase = true),
) {

  val bytes: ByteArray by lazy {
    ResourceUtils.read(location)
  }

}
