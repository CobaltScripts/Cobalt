package org.cobalt.util.render.skia.surface

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.GpuTexture
import com.mojang.blaze3d.vulkan.VulkanDevice
import io.github.humbleui.skija.Canvas
import org.cobalt.mixin.mojang.GpuDeviceAccessor

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
      val rawDevice = RenderSystem.getDevice()
      val backend = (rawDevice as GpuDeviceAccessor).`cobalt$getBackend`()

      if (backend is VulkanDevice) {
        return VulkanSurface()
      }

      return GlSurface()
    }

  }

}
