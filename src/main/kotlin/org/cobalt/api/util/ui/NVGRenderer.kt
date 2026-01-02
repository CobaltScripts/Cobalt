package org.cobalt.api.util.ui

import com.mojang.blaze3d.opengl.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import net.minecraft.client.gl.GlBackend
import net.minecraft.client.texture.GlTexture
import org.cobalt.Cobalt.mc
import org.cobalt.api.util.ui.helper.Font
import org.cobalt.api.util.ui.helper.Gradient
import org.cobalt.api.util.ui.helper.Image
import org.lwjgl.nanovg.NVGColor
import org.lwjgl.nanovg.NVGPaint
import org.lwjgl.nanovg.NanoSVG.*
import org.lwjgl.nanovg.NanoVG.*
import org.lwjgl.nanovg.NanoVGGL3.*
import org.lwjgl.opengl.GL30
import org.lwjgl.stb.STBImage.stbi_load_from_memory
import org.lwjgl.system.MemoryUtil.memAlloc
import org.lwjgl.system.MemoryUtil.memFree

/**
 * A renderer that uses NanoVG for 2D graphics rendering.
 * 
 * This object wraps NanoVG functions to make it easier to draw shapes, text,
 * images, and gradients. All drawing must happen between [beginFrame]
 * and [endFrame] calls.
 * 
 * Implementation from vexel by StellariumMC
 * Original work: https://github.com/StellariumMC/vexel
 *
 * @author StellariumMC Odin Contributors
 */
@Suppress("unused")
object NVGRenderer {

  private val nvgPaint = NVGPaint.malloc()
  private val nvgColor = NVGColor.malloc()
  private val nvgColor2: NVGColor = NVGColor.malloc()
  val interFont = Font("Inter", "/assets/cobalt/fonts/Inter.otf")

  private val images = HashMap<Image, NVGImage>()
  private val fontMap = HashMap<Font, NVGFont>()
  private val fontBounds = FloatArray(4)

  private var scissor: Scissor? = null
  private var drawing: Boolean = false
  private var vg = -1L

  init {
    vg = nvgCreate(NVG_ANTIALIAS or NVG_STENCIL_STROKES)
    require(vg != -1L) { "Failed to initialize NanoVG" }
  }

  /**
   * Starts a new drawing frame. Call this before any drawing operations.
   * 
   * @param width The width of the frame in pixels
   * @param height The height of the frame in pixels
   * @throws IllegalStateException if called while already drawing
   */
  fun beginFrame(width: Float, height: Float) {
    if (drawing) throw IllegalStateException("[NVGRenderer] Already drawing, but called beginFrame")

    val framebuffer = mc.framebuffer
    val glFramebuffer = (framebuffer.colorAttachment as GlTexture).getOrCreateFramebuffer(
      (RenderSystem.getDevice() as GlBackend).bufferManager,
      null
    )

    GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, glFramebuffer)
    GlStateManager._viewport(0, 0, framebuffer.textureWidth, framebuffer.textureHeight)
    GlStateManager._activeTexture(GL30.GL_TEXTURE0)

    nvgBeginFrame(vg, width, height, 1f)
    nvgTextAlign(vg, NVG_ALIGN_LEFT or NVG_ALIGN_TOP)
    drawing = true
  }

  /**
   * Ends the current drawing frame and restores OpenGL state.
   * 
   * @throws IllegalStateException if called when not drawing
   */
  fun endFrame() {
    if (!drawing) throw IllegalStateException("[NVGRenderer] Not drawing, but called endFrame")
    nvgEndFrame(vg)
    GlStateManager._disableCull()
    GlStateManager._disableDepthTest()
    GlStateManager._enableBlend()
    GlStateManager._blendFuncSeparate(770, 771, 1, 0)
    GlStateManager._glUseProgram(0)

    if (TextureTracker.prevActiveTexture != -1) {
      GlStateManager._activeTexture(TextureTracker.prevActiveTexture)
      if (TextureTracker.prevBoundTexture != -1) GlStateManager._bindTexture(TextureTracker.prevBoundTexture)
    }

    GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)
    drawing = false
  }

  /** Saves the current transform state. Use with [pop] to restore it later. */
  fun push() = nvgSave(vg)
  
  /** Restores the transform state saved by the last [push] call. */
  fun pop() = nvgRestore(vg)
  
  /**
   * Scales subsequent drawing operations.
   * 
   * @param x Scale factor on the x-axis
   * @param y Scale factor on the y-axis
   */
  fun scale(x: Float, y: Float) = nvgScale(vg, x, y)
  
  /**
   * Translates (moves) subsequent drawing operations.
   * 
   * @param x Distance to move on the x-axis
   * @param y Distance to move on the y-axis
   */
  fun translate(x: Float, y: Float) = nvgTranslate(vg, x, y)
  
  /**
   * Rotates subsequent drawing operations.
   * 
   * @param amount Rotation amount in radians
   */
  fun rotate(amount: Float) = nvgRotate(vg, amount)
  
  /**
   * Sets the global alpha (transparency) for subsequent drawing operations.
   * 
   * @param amount Alpha value between 0 (fully transparent) and 1 (fully opaque)
   */
  fun globalAlpha(amount: Float) = nvgGlobalAlpha(vg, amount.coerceIn(0f, 1f))

  /**
   * Pushes a scissor region to clip drawing. Only content inside this region will be visible.
   * Call [popScissor] to remove it. Scissors can be nested.
   * 
   * @param x X position of the scissor region
   * @param y Y position of the scissor region
   * @param w Width of the scissor region
   * @param h Height of the scissor region
   */
  fun pushScissor(x: Float, y: Float, w: Float, h: Float) {
    scissor = Scissor(scissor, x, y, w + x, h + y)
    scissor?.applyScissor()
  }

  /**
   * Removes the most recently pushed scissor region.
   */
  fun popScissor() {
    nvgResetScissor(vg)
    scissor = scissor?.previous
    scissor?.applyScissor()
  }

  /**
   * Draws a line between two points.
   * 
   * @param x1 Starting X coordinate
   * @param y1 Starting Y coordinate
   * @param x2 Ending X coordinate
   * @param y2 Ending Y coordinate
   * @param thickness Line thickness in pixels
   * @param color Line color in ARGB format
   */
  fun line(x1: Float, y1: Float, x2: Float, y2: Float, thickness: Float, color: Int) {
    nvgBeginPath(vg)
    nvgMoveTo(vg, x1, y1)
    nvgLineTo(vg, x2, y2)
    nvgStrokeWidth(vg, thickness)
    color(color)
    nvgStrokeColor(vg, nvgColor)
    nvgStroke(vg)
  }

  /**
   * Draws a rectangle with rounded corners on either the top or bottom.
   * 
   * @param x X position
   * @param y Y position
   * @param w Width
   * @param h Height
   * @param color Fill color in ARGB format
   * @param radius Corner radius for the rounded side
   * @param roundTop If true, rounds the top corners; if false, rounds the bottom corners
   */
  fun drawHalfRoundedRect(x: Float, y: Float, w: Float, h: Float, color: Int, radius: Float, roundTop: Boolean) {
    nvgBeginPath(vg)

    if (roundTop) {
      nvgMoveTo(vg, x, y + h)
      nvgLineTo(vg, x + w, y + h)
      nvgLineTo(vg, x + w, y + radius)
      nvgArcTo(vg, x + w, y, x + w - radius, y, radius)
      nvgLineTo(vg, x + radius, y)
      nvgArcTo(vg, x, y, x, y + radius, radius)
      nvgLineTo(vg, x, y + h)
    } else {
      nvgMoveTo(vg, x, y)
      nvgLineTo(vg, x + w, y)
      nvgLineTo(vg, x + w, y + h - radius)
      nvgArcTo(vg, x + w, y + h, x + w - radius, y + h, radius)
      nvgLineTo(vg, x + radius, y + h)
      nvgArcTo(vg, x, y + h, x, y + h - radius, radius)
      nvgLineTo(vg, x, y)
    }

    nvgClosePath(vg)
    color(color)
    nvgFillColor(vg, nvgColor)
    nvgFill(vg)
  }

  /**
   * Draws a filled rectangle with rounded corners.
   * 
   * @param x X position
   * @param y Y position
   * @param w Width
   * @param h Height
   * @param color Fill color in ARGB format
   * @param radius Corner radius
   */
  fun rect(x: Float, y: Float, w: Float, h: Float, color: Int, radius: Float) {
    nvgBeginPath(vg)
    nvgRoundedRect(vg, x, y, w, h + .5f, radius)
    color(color)
    nvgFillColor(vg, nvgColor)
    nvgFill(vg)
  }

  /**
   * Draws a filled rectangle with sharp corners.
   * 
   * @param x X position
   * @param y Y position
   * @param w Width
   * @param h Height
   * @param color Fill color in ARGB format
   */
  fun rect(x: Float, y: Float, w: Float, h: Float, color: Int) {
    nvgBeginPath(vg)
    nvgRect(vg, x, y, w, h + .5f)
    color(color)
    nvgFillColor(vg, nvgColor)
    nvgFill(vg)
  }

  /**
   * Draws a hollow rectangle outline with rounded corners.
   * 
   * @param x X position
   * @param y Y position
   * @param w Width
   * @param h Height
   * @param thickness Border thickness in pixels
   * @param color Border color in ARGB format
   * @param radius Corner radius
   */
  fun hollowRect(x: Float, y: Float, w: Float, h: Float, thickness: Float, color: Int, radius: Float) {
    nvgBeginPath(vg)
    nvgRoundedRect(vg, x, y, w, h, radius)
    nvgStrokeWidth(vg, thickness)
    nvgPathWinding(vg, NVG_HOLE)
    color(color)
    nvgStrokeColor(vg, nvgColor)
    nvgStroke(vg)
  }

  /**
   * Draws a hollow rectangle outline with a gradient stroke.
   * 
   * @param x X position
   * @param y Y position
   * @param w Width
   * @param h Height
   * @param thickness Border thickness in pixels
   * @param color1 Starting gradient color in ARGB format
   * @param color2 Ending gradient color in ARGB format
   * @param gradient Gradient direction
   * @param radius Corner radius
   */
  fun hollowGradientRect(
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    thickness: Float,
    color1: Int,
    color2: Int,
    gradient: Gradient,
    radius: Float,
  ) {
    nvgBeginPath(vg)
    nvgRoundedRect(vg, x, y, w, h, radius)
    nvgStrokeWidth(vg, thickness)
    gradient(color1, color2, x, y, w, h, gradient)
    nvgStrokePaint(vg, nvgPaint)
    nvgStroke(vg)
  }

  /**
   * Draws a filled rectangle with a gradient.
   * 
   * @param x X position
   * @param y Y position
   * @param w Width
   * @param h Height
   * @param color1 Starting gradient color in ARGB format
   * @param color2 Ending gradient color in ARGB format
   * @param gradient Gradient direction
   * @param radius Corner radius
   */
  fun gradientRect(
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    color1: Int,
    color2: Int,
    gradient: Gradient,
    radius: Float,
  ) {
    nvgBeginPath(vg)
    nvgRoundedRect(vg, x, y, w, h, radius)
    gradient(color1, color2, x, y, w, h, gradient)
    nvgFillPaint(vg, nvgPaint)
    nvgFill(vg)
  }

  /**
   * Draws a filled circle.
   * 
   * @param x Center X coordinate
   * @param y Center Y coordinate
   * @param radius Circle radius
   * @param color Fill color in ARGB format
   */
  fun circle(x: Float, y: Float, radius: Float, color: Int) {
    nvgBeginPath(vg)
    nvgCircle(vg, x, y, radius)
    color(color)
    nvgFillColor(vg, nvgColor)
    nvgFill(vg)
  }

  /**
   * Renders text at the specified position.
   * 
   * @param text The text to render
   * @param x X position
   * @param y Y position
   * @param size Font size
   * @param color Text color in ARGB format
   * @param font The font to use (defaults to Inter)
   */
  fun text(text: String, x: Float, y: Float, size: Float, color: Int, font: Font = interFont) {
    nvgFontSize(vg, size)
    nvgFontFaceId(vg, getFontID(font))
    color(color)
    nvgFillColor(vg, nvgColor)
    nvgText(vg, x, y + .5f, text)
  }

  /**
   * Renders text with a drop shadow effect.
   * 
   * @param text The text to render
   * @param x X position
   * @param y Y position
   * @param size Font size
   * @param color Text color in ARGB format
   * @param font The font to use (defaults to Inter)
   */
  fun textShadow(text: String, x: Float, y: Float, size: Float, color: Int, font: Font = interFont) {
    nvgFontFaceId(vg, getFontID(font))
    nvgFontSize(vg, size)
    color(-16777216)
    nvgFillColor(vg, nvgColor)
    nvgText(vg, round(x + 3f), round(y + 3f), text)

    color(color)
    nvgFillColor(vg, nvgColor)
    nvgText(vg, round(x), round(y), text)
  }

  /**
   * Calculates the width of the given text when rendered.
   * 
   * @param text The text to measure
   * @param size Font size
   * @param font The font to use (defaults to Inter)
   * @return The width in pixels
   */
  fun textWidth(text: String, size: Float, font: Font = interFont): Float {
    nvgFontSize(vg, size)
    nvgFontFaceId(vg, getFontID(font))
    return nvgTextBounds(vg, 0f, 0f, text, fontBounds)
  }

  /**
   * Renders text that automatically wraps to fit within a specified width.
   * 
   * @param text The text to render
   * @param x X position
   * @param y Y position
   * @param w Maximum width before wrapping
   * @param size Font size
   * @param color Text color in ARGB format
   * @param font The font to use
   * @param lineHeight Line height multiplier (1.0 = normal spacing)
   */
  fun drawWrappedString(
    text: String,
    x: Float,
    y: Float,
    w: Float,
    size: Float,
    color: Int,
    font: Font,
    lineHeight: Float = 1f,
  ) {
    nvgFontSize(vg, size)
    nvgFontFaceId(vg, getFontID(font))
    nvgTextLineHeight(vg, lineHeight)
    color(color)
    nvgFillColor(vg, nvgColor)
    nvgTextBox(vg, x, y, w, text)
  }

  /**
   * Calculates the bounds of wrapped text.
   * 
   * @param text The text to measure
   * @param w Maximum width before wrapping
   * @param size Font size
   * @param font The font to use
   * @param lineHeight Line height multiplier
   * @return Array containing [minX, minY, maxX, maxY]
   */
  fun wrappedTextBounds(
    text: String,
    w: Float,
    size: Float,
    font: Font,
    lineHeight: Float = 1f,
  ): FloatArray {
    val bounds = FloatArray(4)
    nvgFontSize(vg, size)
    nvgFontFaceId(vg, getFontID(font))
    nvgTextLineHeight(vg, lineHeight)
    nvgTextBoxBounds(vg, 0f, 0f, w, text, bounds)
    return bounds
  }

  /**
   * Creates a NanoVG image from an existing OpenGL texture.
   * 
   * @param textureId The OpenGL texture ID
   * @param textureWidth Width of the texture
   * @param textureHeight Height of the texture
   * @return NanoVG image handle
   */
  fun createNVGImage(textureId: Int, textureWidth: Int, textureHeight: Int): Int =
    nvglCreateImageFromHandle(
      vg,
      textureId,
      textureWidth,
      textureHeight,
      NVG_IMAGE_NEAREST or NVG_IMAGE_NODELETE
    )

  /**
   * Draws an image at the specified position and size.
   * 
   * @param image The image to draw
   * @param x X position
   * @param y Y position
   * @param w Width to render
   * @param h Height to render
   * @param radius Corner radius for rounded image (0 = sharp corners)
   * @param colorMask Color tint/mask to apply (0 = no tint)
   */
  fun image(image: Image, x: Float, y: Float, w: Float, h: Float, radius: Float = 0F, colorMask: Int = 0) {
    nvgImagePattern(vg, x, y, w, h, 0f, getImage(image), 1f, nvgPaint)

    if (colorMask != 0) {
      nvgRGBA(
        colorMask.red.toByte(),
        colorMask.green.toByte(),
        colorMask.blue.toByte(),
        colorMask.alpha.toByte(),
        nvgPaint.innerColor()
      )
    }

    nvgBeginPath(vg)

    if (radius == 0F)
      nvgRect(vg, x, y, w, h + .5f)
    else
      nvgRoundedRect(vg, x, y, w, h + .5f, radius)

    nvgFillPaint(vg, nvgPaint)
    nvgFill(vg)
  }

  /**
   * Loads an image from a resource path. Supports both PNG/JPG and SVG formats.
   * Images are reference-counted, so the same image can be loaded multiple times safely.
   * 
   * @param resourcePath Path to the image resource
   * @return The loaded image
   */
  fun createImage(resourcePath: String): Image {
    val image = images.keys.find { it.identifier == resourcePath } ?: Image(resourcePath)
    if (image.isSVG) images.getOrPut(image) { NVGImage(0, loadSVG(image)) }.count++
    else images.getOrPut(image) { NVGImage(0, loadImage(image)) }.count++
    return image
  }

  /**
   * Deletes an image and frees its resources. Uses reference counting, so the image
   * is only truly deleted when all references are released.
   * 
   * @param image The image to delete
   */
  fun deleteImage(image: Image) {
    val nvgImage = images[image] ?: return
    nvgImage.count--
    if (nvgImage.count == 0) {
      nvgDeleteImage(vg, nvgImage.nvg)
      images.remove(image)
    }
  }

  private fun getImage(image: Image): Int {
    return images[image]?.nvg ?: throw IllegalStateException("Image (${image.identifier}) doesn't exist")
  }

  private fun loadImage(image: Image): Int {
    val w = IntArray(1)
    val h = IntArray(1)
    val channels = IntArray(1)
    val buffer = stbi_load_from_memory(
      image.buffer(),
      w,
      h,
      channels,
      4
    ) ?: throw NullPointerException("Failed to load image: ${image.identifier}")
    return nvgCreateImageRGBA(vg, w[0], h[0], 0, buffer)
  }

  private fun loadSVG(image: Image): Int {
    val vec = image.stream.use { it.bufferedReader().readText() }
    val svg = nsvgParse(vec, "px", 96f) ?: throw IllegalStateException("Failed to parse ${image.identifier}")

    val width = svg.width().toInt()
    val height = svg.height().toInt()
    val buffer = memAlloc(width * height * 4)

    try {
      val rasterizer = nsvgCreateRasterizer()
      nsvgRasterize(rasterizer, svg, 0f, 0f, 1f, buffer, width, height, width * 4)
      val nvgImage = nvgCreateImageRGBA(vg, width, height, 0, buffer)
      nsvgDeleteRasterizer(rasterizer)
      return nvgImage
    } finally {
      nsvgDelete(svg)
      memFree(buffer)
    }
  }

  private fun color(color: Int) {
    nvgRGBA(color.red.toByte(), color.green.toByte(), color.blue.toByte(), color.alpha.toByte(), nvgColor)
  }

  private fun color(color1: Int, color2: Int) {
    nvgRGBA(
      color1.red.toByte(),
      color1.green.toByte(),
      color1.blue.toByte(),
      color1.alpha.toByte(),
      nvgColor
    )
    nvgRGBA(
      color2.red.toByte(),
      color2.green.toByte(),
      color2.blue.toByte(),
      color2.alpha.toByte(),
      nvgColor2
    )
  }

  private fun gradient(color1: Int, color2: Int, x: Float, y: Float, w: Float, h: Float, direction: Gradient) {
    color(color1, color2)
    when (direction) {
      Gradient.LeftToRight -> nvgLinearGradient(vg, x, y, x + w, y, nvgColor, nvgColor2, nvgPaint)
      Gradient.TopToBottom -> nvgLinearGradient(vg, x, y, x, y + h, nvgColor, nvgColor2, nvgPaint)
      Gradient.TopLeftToBottomRight -> nvgLinearGradient(vg, x, y, x + w, y + h, nvgColor, nvgColor2, nvgPaint)
    }
  }

  private fun getFontID(font: Font): Int {
    return fontMap.getOrPut(font) {
      val buffer = font.buffer()
      NVGFont(
        nvgCreateFontMem(
          vg,
          font.name,
          buffer,
          false
        ), buffer
      )
    }.id
  }

  private class Scissor(val previous: Scissor?, val x: Float, val y: Float, val maxX: Float, val maxY: Float) {
    fun applyScissor() {
      if (previous == null) nvgScissor(vg, x, y, maxX - x, maxY - y)
      else {
        val x = max(x, previous.x)
        val y = max(y, previous.y)
        val width = max(0f, (min(maxX, previous.maxX) - x))
        val height = max(0f, (min(maxY, previous.maxY) - y))
        nvgScissor(vg, x, y, width, height)
      }
    }
  }

  private data class NVGImage(var count: Int, val nvg: Int)
  private data class NVGFont(val id: Int, val buffer: ByteBuffer)

  inline val Int.red get() = this shr 16 and 0xFF
  inline val Int.green get() = this shr 8 and 0xFF
  inline val Int.blue get() = this and 0xFF
  inline val Int.alpha get() = this shr 24 and 0xFF

}