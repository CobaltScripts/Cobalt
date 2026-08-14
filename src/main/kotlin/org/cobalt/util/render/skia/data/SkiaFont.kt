package org.cobalt.util.render.skia.data

import org.cobalt.util.web.ResourceUtils

data class SkiaFont(val location: String) {

  val bytes: ByteArray by lazy {
    ResourceUtils.read(location)
  }

}
