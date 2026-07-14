package org.cobalt.util.render.skia

import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI
import java.nio.file.Files
import kotlinx.coroutines.runBlocking
import org.cobalt.Cobalt

object ResourceUtils {

  @JvmStatic
  fun read(location: String): ByteArray {
    val trimmedPath = location.trim()

    return if (trimmedPath.startsWith("http")) {
      runBlocking {
        getInputStream(trimmedPath).readBytes()
      }
    } else {
      val file = File(trimmedPath)

      if (file.exists() && file.isFile) {
        Files.readAllBytes(file.toPath())
      } else {
        this::class.java.getResourceAsStream(trimmedPath)?.readBytes() ?: throw FileNotFoundException(trimmedPath)
      }
    }
  }

  @JvmStatic
  fun getInputStream(url: String, timeout: Int = 5000, cache: Boolean = true): InputStream {
    val connection = (URI(url).toURL().openConnection() as HttpURLConnection).apply {
      requestMethod = "GET"
      useCaches = cache
      connectTimeout = timeout
      readTimeout = timeout
      setRequestProperty("User-Agent", Cobalt.MOD_NAME)
    }

    return connection.inputStream
  }

}
