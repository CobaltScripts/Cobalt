package org.cobalt.util.skia.surface

import com.mojang.blaze3d.opengl.GlStateManager
import com.mojang.blaze3d.opengl.GlTexture
import com.mojang.blaze3d.textures.GpuTexture
import io.github.humbleui.skija.*
import org.cobalt.util.skia.Skia
import org.lwjgl.opengl.GL11C
import org.lwjgl.opengl.GL30C
import org.lwjgl.opengl.GL33C

internal class GlSurface : SkiaSurface {

  private var context: DirectContext? = null
  private var renderTarget: BackendRenderTarget? = null
  private var surface: Surface? = null

  private var fbo = 0
  private var depthStencil = 0
  private var attachedWidth = 0
  private var attachedHeight = 0
  private var lastTextureId = 0

  override fun render(
    width: Int,
    height: Int,
    texture: GpuTexture,
    draw: (Canvas) -> Unit,
  ) {
    val colorTexId = (texture as? GlTexture)?.glId() ?: return
    val previousFbo = GL11C.glGetInteger(GL30C.GL_FRAMEBUFFER_BINDING)
    val previousViewport = IntArray(4)
    GL11C.glGetIntegerv(GL11C.GL_VIEWPORT, previousViewport)

    bindTarget(colorTexId, width, height)

    GlStateManager._viewport(0, 0, width, height)
    GL33C.glBindSampler(0, 0)

    val directContext = context ?: DirectContext.makeGL().also { context = it }
    directContext.resetGLAll()

    val skijaSurface = surfaceFor(width, height, colorTexId)

    Skia.beginFrame(skijaSurface.canvas)

    try {
      draw(skijaSurface.canvas)
    } finally {
      Skia.endFrame()
    }

    directContext.flushAndSubmit(skijaSurface, true)

    GL30C.glBindVertexArray(0)
    GL30C.glUseProgram(0)

    GlStateManager._disableDepthTest()
    GlStateManager._disableCull()
    GlStateManager._enableBlend(0)
    GlStateManager._blendFuncSeparate(770, 771, 1, 0)

    GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, previousFbo)
    GlStateManager._viewport(previousViewport[0], previousViewport[1], previousViewport[2], previousViewport[3])
  }

  private fun bindTarget(colorTexId: Int, width: Int, height: Int) {
    if (fbo == 0) {
      fbo = GlStateManager.glGenFramebuffers()
    }

    GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, fbo)
    GlStateManager._glFramebufferTexture2D(
      GL30C.GL_FRAMEBUFFER,
      GL30C.GL_COLOR_ATTACHMENT0,
      GL11C.GL_TEXTURE_2D,
      colorTexId,
      0
    )

    if (depthStencil == 0 || attachedWidth != width || attachedHeight != height) {
      if (depthStencil != 0) {
        GL30C.glDeleteRenderbuffers(depthStencil)
      }

      depthStencil = GL30C.glGenRenderbuffers()

      GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, depthStencil)
      GL30C.glRenderbufferStorage(GL30C.GL_RENDERBUFFER, GL30C.GL_DEPTH24_STENCIL8, width, height)
      GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, 0)
      GL30C.glFramebufferRenderbuffer(
        GL30C.GL_FRAMEBUFFER,
        GL30C.GL_DEPTH_STENCIL_ATTACHMENT,
        GL30C.GL_RENDERBUFFER,
        depthStencil
      )

      attachedWidth = width
      attachedHeight = height
    }
  }

  private fun surfaceFor(width: Int, height: Int, textureId: Int): Surface {
    val existing = surface

    if (existing != null && existing.width == width && existing.height == height && lastTextureId == textureId) {
      return existing
    }

    surface?.close()
    renderTarget?.close()

    val directContext = context ?: DirectContext.makeGL().also { context = it }
    val target = BackendRenderTarget.makeGL(width, height, 0, 8, fbo, GL30C.GL_RGBA8)
    val created = Surface.wrapBackendRenderTarget(
      directContext,
      target,
      SurfaceOrigin.BOTTOM_LEFT,
      ColorType.RGBA_8888,
      ColorSpace.getSRGB()
    )

    renderTarget = target
    surface = created
    lastTextureId = textureId

    return created
  }

  override fun close() {
    surface?.close()
    surface = null
    renderTarget?.close()
    renderTarget = null

    if (depthStencil != 0) {
      GL30C.glDeleteRenderbuffers(depthStencil)
      depthStencil = 0
    }

    if (fbo != 0) {
      GlStateManager._glDeleteFramebuffers(fbo)
      fbo = 0
    }

    context?.close()
    context = null
  }

}
