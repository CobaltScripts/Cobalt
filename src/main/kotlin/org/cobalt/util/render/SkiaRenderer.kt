@file:Suppress("TooManyFunctions")

package org.cobalt.util.render

import io.github.humbleui.skija.*
import io.github.humbleui.skija.svg.SVGDOM
import io.github.humbleui.skija.svg.SVGLengthContext
import io.github.humbleui.types.RRect
import io.github.humbleui.types.Rect
import java.awt.Color
import kotlin.math.max
import org.cobalt.util.render.skia.data.SkiaCorner
import org.cobalt.util.render.skia.data.SkiaFont
import org.cobalt.util.render.skia.data.SkiaImage
import org.cobalt.util.render.skia.helper.LineWrapper

object SkiaRenderer {

  private val imageCache = HashMap<SkiaImage, CachedImage>()
  private val fonts = HashMap<FontKey, Font>()
  private val typefaces = HashMap<SkiaFont, Typeface>()
  private var canvas: Canvas? = null
  private var globalAlpha = 1f

  @JvmStatic
  val regularFont = SkiaFont("/assets/cobalt/fonts/ProductSans-Regular.ttf")

  @JvmStatic
  val boldFont = SkiaFont("/assets/cobalt/fonts/ProductSans-Bold.ttf")

  internal fun beginFrame(canvas: Canvas) {
    this.canvas = canvas
    this.globalAlpha = 1f
  }

  internal fun endFrame() {
    canvas = null
    globalAlpha = 1f
  }

  @JvmStatic
  fun push() {
    canvas().save()
  }

  @JvmStatic
  fun pop() {
    canvas().restore()
  }

  @JvmStatic
  fun scale(x: Float, y: Float) {
    canvas().scale(x, y)
  }

  @JvmStatic
  fun translate(x: Float, y: Float) {
    canvas().translate(x, y)
  }

  @JvmStatic
  fun rotate(degrees: Float) {
    canvas().rotate(degrees)
  }

  @JvmStatic
  fun globalAlpha(amount: Float) {
    globalAlpha = amount.coerceIn(0f, 1f)
  }

  @JvmStatic
  fun pushScissor(x: Float, y: Float, width: Float, height: Float, radius: Float = 0f) {
    canvas().save()
    canvas().clipRRect(RRect.makeXYWH(x, y, width, height, radius), ClipMode.INTERSECT, true)
  }

  @JvmStatic
  fun popScissor() {
    canvas().restore()
  }

  @JvmStatic
  fun line(x1: Float, y1: Float, x2: Float, y2: Float, thickness: Float, color: Color) {
    paint(color, PaintMode.STROKE).use {
      it.strokeWidth = thickness
      it.strokeCap = PaintStrokeCap.ROUND
      canvas().drawLine(x1, y1, x2, y2, it)
    }
  }

  @JvmStatic
  fun circle(x: Float, y: Float, radius: Float, color: Color) {
    paint(color).use {
      canvas().drawCircle(x, y, radius, it)
    }
  }

  @JvmStatic
  fun rect(x: Float, y: Float, width: Float, height: Float, color: Color) {
    paint(color).use {
      canvas().drawRect(Rect.makeXYWH(x, y, width, height), it)
    }
  }

  @JvmStatic
  fun outline(x: Float, y: Float, width: Float, height: Float, thickness: Float, color: Color) {
    paint(color, PaintMode.STROKE).use {
      it.strokeWidth = thickness
      canvas().drawRect(Rect.makeXYWH(x, y, width, height), it)
    }
  }

  @JvmStatic
  fun roundedRect(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    radius: Float,
    color: Color,
    corners: Array<SkiaCorner> = SkiaCorner.ALL,
  ) {
    val radii = radii(radius, corners)

    paint(color).use {
      canvas().drawRRect(RRect.makeComplexXYWH(x, y, width, height, radii), it)
    }
  }

  @JvmStatic
  fun roundedOutline(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    thickness: Float,
    radius: Float,
    color: Color,
    corners: Array<SkiaCorner> = SkiaCorner.ALL,
  ) {
    val radii = radii(radius, corners)

    paint(color, PaintMode.STROKE).use {
      it.strokeWidth = thickness
      canvas().drawRRect(RRect.makeComplexXYWH(x, y, width, height, radii), it)
    }
  }

  @JvmStatic
  fun text(font: SkiaFont, text: String, x: Float, y: Float, size: Float, color: Color) {
    paint(color).use { fill ->
      val skijaFont = font(font, size)
      canvas().drawString(text, x, y - skijaFont.metrics.ascent, skijaFont, fill)
    }
  }

  @JvmStatic
  fun textWidth(font: SkiaFont, text: String, size: Float): Float {
    return font(font, size).measureTextWidth(text)
  }

  @JvmStatic
  fun wrappedText(
    font: SkiaFont,
    text: String,
    x: Float,
    y: Float,
    width: Float,
    size: Float,
    color: Color,
    lineHeight: Float = 1f,
  ) {
    var cursorY = y
    val spacing = size * lineHeight

    wrap(font, text, width, size).forEach { line ->
      text(font, line, x, cursorY, size, color)
      cursorY += spacing
    }
  }

  @JvmStatic
  fun wrappedTextHeight(
    font: SkiaFont,
    text: String,
    maxWidth: Float,
    fontSize: Float,
    lineHeight: Float = 1f,
  ): Float {
    val lines = wrap(font, text, maxWidth, fontSize)
    return lines.size * (fontSize * lineHeight)
  }

  @JvmStatic
  fun wrap(font: SkiaFont, text: String, maxWidth: Float, size: Float): List<String> {
    val wrapper = LineWrapper(maxWidth) { stringToMeasure ->
      textWidth(font, stringToMeasure, size)
    }

    return wrapper.wrap(text)
  }

  @JvmStatic
  fun createImage(resourcePath: String): SkiaImage {
    val image = imageCache.keys.find { it.location == resourcePath } ?: SkiaImage(resourcePath)
    val cached = imageCache.getOrPut(image) { CachedImage(0, loadImage(image)) }
    cached.count++
    return image
  }

  @JvmStatic
  fun deleteImage(image: SkiaImage) {
    val cached = imageCache[image] ?: return
    cached.count--

    if (cached.count > 0) {
      return
    }

    cached.image.close()
    imageCache.remove(image)
  }

  @JvmStatic
  fun image(
    image: SkiaImage,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    radius: Float? = null,
    color: Color? = null,
  ) {
    val img = getImage(image)
    val dst = Rect.makeXYWH(x, y, width, height)

    Paint().use { paint ->
      paint.isAntiAlias = true

      if (color != null && image.isSvg) {
        paint.colorFilter = ColorFilter.makeBlend(color.rgb, BlendMode.MODULATE)
      }

      val src = Rect.makeWH(img.width.toFloat(), img.height.toFloat())

      paint.alphaf = globalAlpha
      canvas().save()

      try {
        canvas().clipRRect(
          RRect.makeXYWH(x, y, width, height, radius ?: 0f),
          ClipMode.INTERSECT,
          true
        )

        canvas().drawImageRect(img, src, dst, SamplingMode.MITCHELL, paint, true)
      } finally {
        canvas().restore()
      }
    }
  }

  @JvmStatic
  fun blurredImage(
    image: SkiaImage,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    radius: Float,
    cornerRadius: Float = 0f,
  ) {
    val img = getImage(image)
    val src = Rect.makeWH(img.width.toFloat(), img.height.toFloat())
    val dst = Rect.makeXYWH(x, y, width, height)

    val blurPaint = Paint().apply {
      imageFilter = ImageFilter.makeBlur(radius, radius, FilterTileMode.REPEAT)
      isAntiAlias = true
      alphaf = globalAlpha
    }

    canvas().save()
    try {
      canvas().clipRRect(
        RRect.makeXYWH(x, y, width, height, cornerRadius),
        ClipMode.INTERSECT,
        true
      )

      canvas().drawImageRect(img, src, dst, SamplingMode.MITCHELL, blurPaint, true)
    } finally {
      canvas().restore()
      blurPaint.close()
    }
  }

  @JvmStatic
  fun imageSize(image: SkiaImage): Pair<Int, Int> {
    val img = getImage(image)
    return img.width to img.height
  }

  private fun getImage(image: SkiaImage): Image {
    return imageCache[image]?.image ?: throw IllegalStateException("Image (${image.location}) doesn't exist")
  }

  private fun loadImage(image: SkiaImage): Image {
    if (!image.isSvg) {
      return Image.makeDeferredFromEncodedBytes(image.bytes)
    }

    val data = Data.makeFromBytes(image.bytes)
    val dom = SVGDOM(data)
    val root = dom.root
      ?: throw IllegalStateException("Failed to read SVG root: ${image.location}")

    val intrinsic = root.getIntrinsicSize(SVGLengthContext(256f, 256f, 96f))
    val width = max(1, intrinsic.x.toInt())
    val height = max(1, intrinsic.y.toInt())

    val surface = Surface.makeRaster(
      ImageInfo(width, height, ColorType.N32, ColorAlphaType.PREMUL, ColorSpace.getSRGB())
    )

    surface.canvas.clear(0)
    dom.setContainerSize(width.toFloat(), height.toFloat())
    dom.render(surface.canvas)

    val snapshot = surface.makeImageSnapshot()

    surface.close()
    dom.close()
    data.close()

    return snapshot
  }

  private fun font(font: SkiaFont, size: Float): Font {
    return fonts.getOrPut(FontKey(font, size)) {
      Font(typefaces.getOrPut(font) {
        FontMgr.getDefault().makeFromData(Data.makeFromBytes(font.bytes))
          ?: error("Failed to load font ('${font.location})")
      }, size).setEdging(FontEdging.SUBPIXEL_ANTI_ALIAS)
    }
  }

  private fun paint(color: Color, mode: PaintMode = PaintMode.FILL): Paint {
    return Paint()
      .setAntiAlias(true)
      .setMode(mode)
      .setColor(color.rgb)
      .setAlphaf((color.alpha / 255f) * globalAlpha)
  }

  private fun radii(radius: Float, corners: Array<SkiaCorner>): FloatArray {
    fun has(corner: SkiaCorner) =
      corner in corners

    return floatArrayOf(
      if (has(SkiaCorner.TOP_LEFT)) radius else 0f,
      if (has(SkiaCorner.TOP_LEFT)) radius else 0f,
      if (has(SkiaCorner.TOP_RIGHT)) radius else 0f,
      if (has(SkiaCorner.TOP_RIGHT)) radius else 0f,
      if (has(SkiaCorner.BOTTOM_RIGHT)) radius else 0f,
      if (has(SkiaCorner.BOTTOM_RIGHT)) radius else 0f,
      if (has(SkiaCorner.BOTTOM_LEFT)) radius else 0f,
      if (has(SkiaCorner.BOTTOM_LEFT)) radius else 0f,
    )
  }

  private fun canvas(): Canvas {
    return canvas ?: throw IllegalStateException("Skia frame has not started")
  }

  private data class CachedImage(var count: Int, val image: Image)
  private data class FontKey(val font: SkiaFont, val size: Float)

}
