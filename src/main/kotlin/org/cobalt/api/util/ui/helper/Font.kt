package org.cobalt.api.util.ui.helper

import java.io.FileNotFoundException
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Represents a font that can be loaded from resources and used with NanoVG rendering.
 *
 * This class handles loading font files (like .ttf, .otf) from the classpath and
 * converting them into ByteBuffers that NanoVG can use. Each call to [buffer] creates
 * a new direct ByteBuffer with native byte ordering for optimal performance.
 *
 * Implementation from OdinFabric by odtheking
 * Original work: https://github.com/odtheking/OdinFabric
 *
 * @param name The display name of the font (used for identification and hashing)
 * @param resourcePath The classpath resource path to the font file (e.g., "/assets/fonts/Inter.otf")
 *
 * @author Odin Contributors
 */
class Font(val name: String, private val resourcePath: String) {

  private val cachedBytes: ByteArray? = null

  /**
   * Creates a new ByteBuffer containing the font data.
   *
   * This method loads the font file from resources (if not already cached) and wraps
   * it in a direct ByteBuffer with native byte ordering. Note that this creates a new
   * buffer each time it's called, so you should cache the result if you need to use
   * it multiple times.
   *
   * The ByteBuffer is allocated as a direct buffer, which means it uses off-heap memory
   * for better performance when passing to native libraries like NanoVG.
   *
   * @return A new direct ByteBuffer containing the font data in native byte order
   * @throws FileNotFoundException if the font resource cannot be found at the specified path
   */
  fun buffer(): ByteBuffer {
    val bytes = cachedBytes ?: run {
      val stream = this::class.java.getResourceAsStream(resourcePath) ?: throw FileNotFoundException(resourcePath)
      stream.use { it.readBytes() }
    }
    return ByteBuffer.allocateDirect(bytes.size)
      .order(ByteOrder.nativeOrder())
      .put(bytes)
      .flip() as ByteBuffer
  }

  override fun hashCode(): Int {
    return name.hashCode()
  }

  override fun equals(other: Any?): Boolean {
    return other is Font && name == other.name
  }

}
