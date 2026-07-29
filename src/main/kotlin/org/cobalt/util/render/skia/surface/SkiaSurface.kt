package org.cobalt.util.render.skia.surface

import com.mojang.blaze3d.textures.GpuTexture
import io.github.humbleui.skija.Canvas

interface SkiaSurface {

  fun render(
    width: Int,
    height: Int,
    texture: GpuTexture,
    draw: (Canvas) -> Unit,
  )

  fun close()

  companion object {

    fun getInstance(): SkiaSurface {
      return GlSurface()
    }

  }

}
