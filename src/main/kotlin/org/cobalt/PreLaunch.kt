package org.cobalt

import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import org.cobalt.addon.AddonManager
import org.slf4j.LoggerFactory

class PreLaunch : PreLaunchEntrypoint {

  private val logger =
    LoggerFactory.getLogger(this::class.java)

  private val cacheDir = FabricLoader.getInstance()
    .gameDir
    .resolve("config/cobalt/cache")

  override fun onPreLaunch() {
    loadSkia()
    AddonManager.onPreLaunch()
  }

  private fun loadSkia() {
    logger.info("Initializing Skija native bootstrap.")

    val artifactId = resolvePlatformArtifact()
    val nativeJar = downloadIfMissing(artifactId)

    FabricLauncherBase.getLauncher().addToClassPath(nativeJar)

    logger.info("Added Skija native library to classpath.")
  }

  private fun resolvePlatformArtifact(): String {
    val os = System.getProperty("os.name").lowercase()
    val arch = System.getProperty("os.arch").lowercase()

    return when {
      os.contains("win") -> "skija-windows-x64"
      os.contains("mac") && arch.contains("aarch64") -> "skija-macos-arm64"
      os.contains("mac") -> "skija-macos-x64"
      arch.contains("aarch64") -> "skija-linux-arm64"
      else -> "skija-linux-x64"
    }
  }

  private fun downloadIfMissing(artifactId: String): Path {
    Files.createDirectories(cacheDir)

    val fileName = "$artifactId-$SKIJA_VERSION.jar"
    val target = cacheDir.resolve(fileName)

    if (Files.isRegularFile(target)) {
      logger.info("Using cached Skija native library: {}", fileName)
      return target
    }

    logger.info("Downloading Skija native library: {}", artifactId)

    val url =
      "https://repo1.maven.org/maven2/io/github/humbleui/" +
        "$artifactId/$SKIJA_VERSION/$fileName"

    val tmp = Files.createTempFile(cacheDir, "skija-", ".tmp")

    try {
      URI(url).toURL().openStream().use { input ->
        Files.copy(input, tmp, StandardCopyOption.REPLACE_EXISTING)
      }

      Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING)
    } finally {
      Files.deleteIfExists(tmp)
    }

    return target
  }

  companion object {
    private const val SKIJA_VERSION = "0.143.11"
  }

}
