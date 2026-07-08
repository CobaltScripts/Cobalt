package org.cobalt.util.skia.surface

import com.mojang.blaze3d.GpuFormat
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.GpuTexture
import com.mojang.blaze3d.vulkan.VulkanDevice
import com.mojang.blaze3d.vulkan.VulkanGpuTexture
import io.github.humbleui.skija.*
import org.cobalt.mixin.mojang.GpuDeviceAccessor
import org.cobalt.util.skia.Skia
import org.lwjgl.vulkan.VK
import org.lwjgl.vulkan.VK12.*

internal class VulkanSurface : SkiaSurface {

  private var context: DirectContext? = null
  private var renderTarget: BackendRenderTarget? = null
  private var surface: Surface? = null

  private var cachedWidth = 0
  private var cachedHeight = 0
  private var cachedVkImage = 0L

  override fun render(
    width: Int,
    height: Int,
    texture: GpuTexture,
    draw: (Canvas) -> Unit,
  ) {
    val vkTexture = texture as? VulkanGpuTexture ?: return
    val vkImage = vkTexture.vkImage()
    val vkFormat = gpuFormatToVkFormat(vkTexture.format)

    val directContext = context ?: makeContext().also { context = it }

    RenderSystem.getDevice().createCommandEncoder().submit()
    directContext.resetAll()

    val skijaSurface = surfaceFor(directContext, width, height, vkImage, vkFormat)

    Skia.beginFrame(skijaSurface.canvas)

    try {
      draw(skijaSurface.canvas)
    } finally {
      Skia.endFrame()
    }

    directContext.flushAndSubmit(skijaSurface, false)
  }

  private fun makeContext(): DirectContext {
    val vkDevice = (RenderSystem.getDevice() as GpuDeviceAccessor).getBackend() as VulkanDevice

    val lwjglDevice = vkDevice.vkDevice()
    val device = lwjglDevice.address()
    val physDevice = lwjglDevice.physicalDevice.address()
    val instance = vkDevice.instance().vkInstance().address()

    val graphicsQueue = vkDevice.graphicsQueue()
    val queue = graphicsQueue.vkQueue().address()
    val queueFamilyIndex = graphicsQueue.queueFamilyIndex()

    val provider = VK.getFunctionProvider()
    val instanceProcAddr = provider.getFunctionAddress("vkGetInstanceProcAddr")
    val deviceProcAddr = provider.getFunctionAddress("vkGetDeviceProcAddr")

    return DirectContext.makeVulkan(
      instance,
      physDevice,
      device,
      queue,
      queueFamilyIndex,
      instanceProcAddr,
      deviceProcAddr,
      VK_API_VERSION_1_2,
    )
  }

  private fun surfaceFor(
    directContext: DirectContext,
    width: Int,
    height: Int,
    vkImage: Long,
    vkFormat: Int,
  ): Surface {
    val existing = surface

    if (existing != null && cachedWidth == width && cachedHeight == height && cachedVkImage == vkImage) {
      return existing
    }

    surface?.close()
    renderTarget?.close()

    val rt = BackendRenderTarget.makeVulkan(
      width, height,
      vkImage,
      VK_IMAGE_TILING_OPTIMAL,
      VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
      vkFormat,
      VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT or
        VK_IMAGE_USAGE_TRANSFER_DST_BIT or
        VK_IMAGE_USAGE_SAMPLED_BIT or
        VK_IMAGE_USAGE_TRANSFER_SRC_BIT,
      1, 1,
    )

    val surf = Surface.wrapBackendRenderTarget(
      directContext, rt,
      SurfaceOrigin.BOTTOM_LEFT,
      vkFormatToColorType(vkFormat),
      ColorSpace.getSRGB(),
    )

    renderTarget = rt
    surface = surf
    cachedWidth = width
    cachedHeight = height
    cachedVkImage = vkImage

    return surf
  }

  override fun close() {
    surface?.close()
    surface = null

    renderTarget?.close()
    renderTarget = null

    context?.close()
    context = null
  }

  companion object {
    private fun gpuFormatToVkFormat(format: GpuFormat): Int = when (format) {
      GpuFormat.RGBA8_UNORM -> VK_FORMAT_R8G8B8A8_UNORM
      GpuFormat.RGBA8_SNORM -> VK_FORMAT_R8G8B8A8_SNORM
      else -> VK_FORMAT_R8G8B8A8_UNORM
    }

    private fun vkFormatToColorType(vkFormat: Int): ColorType = when (vkFormat) {
      44, 50 -> ColorType.BGRA_8888
      else -> ColorType.RGBA_8888
    }
  }

}
