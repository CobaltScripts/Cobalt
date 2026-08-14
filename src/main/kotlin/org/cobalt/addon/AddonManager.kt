package org.cobalt.addon

import com.google.gson.Gson
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipFile
import kotlin.io.path.extension
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import org.slf4j.LoggerFactory
import org.spongepowered.asm.mixin.Mixins

object AddonManager {

  private val logger = LoggerFactory.getLogger(this::class.java)
  private val gson = Gson()

  @JvmStatic
  val addons = mutableListOf<Pair<AddonMetadata, Addon>>()

  @JvmStatic
  internal fun loadAddons() {
    if (FabricLauncherBase.getLauncher().isDevelopment) {
      loadDevelopmentAddons()
    }

    val folder = FabricLoader.getInstance()
      .gameDir
      .resolve("config/cobalt/addons")
      .apply(Files::createDirectories)

    Files.list(folder).use { paths ->
      paths
        .filter { Files.isRegularFile(it) && it.extension == "jar" }
        .forEach { jarPath ->
          runCatching {
            FabricLauncherBase.getLauncher().addToClassPath(jarPath)
            loadAddon(jarPath)
          }.onFailure { exception ->
            logger.error("Failed to load addon: $jarPath", exception)
          }
        }
    }

    for (pair in addons) {
      val addon = pair.second
      addon.onLoad()
    }
  }

  private fun loadAddon(jarPath: Path) {
    ZipFile(jarPath.toFile()).use { zip ->
      val jsonEntry = checkNotNull(zip.getEntry("cobalt.addon.json")) {
        "Missing cobalt.addon.json in $jarPath"
      }

      val metadata = zip.getInputStream(jsonEntry).use {
        gson.fromJson(it.reader(), AddonMetadata::class.java)
      }

      require(metadata.entrypoints.isNotEmpty()) {
        "Addon ${metadata.id} has no entry points defined"
      }

      synchronized(Mixins::class.java) {
        metadata.mixins.forEach(Mixins::addConfiguration)
      }

      for (entrypoint in metadata.entrypoints) {
        val classPath = "${entrypoint.replace('.', '/')}.class"

        checkNotNull(zip.getEntry(classPath)) {
          "Entrypoint class '$entrypoint' does not exist inside ${jarPath.fileName}"
        }

        val clazz = Class.forName(entrypoint)
        val instance = runCatching {
          clazz.getField("INSTANCE").get(null)
        }.getOrElse {
          clazz
            .getDeclaredConstructor()
            .apply { isAccessible = true }
            .newInstance()
        }

        require(instance is Addon) {
          "Entrypoint '$entrypoint' must implement Addon"
        }

        addons.add(metadata to instance)
      }
    }
  }

  private fun loadDevelopmentAddons() {
    val entries = FabricLoader.getInstance().getEntrypointContainers("cobalt", Addon::class.java)

    for (entry in entries) {
      val metadata = entry.provider.metadata
      val addonMetadata = AddonMetadata(
        id = metadata.id,
        name = metadata.name,
        version = metadata.version?.toString() ?: "unknown",
        entrypoints = listOf(entry.entrypoint.javaClass.name),
        mixins = listOf()
      )

      val addonInstance: Addon = try {
        entry.entrypoint
      } catch (exception: Exception) {
        logger.error("Failed to initialize ${metadata.name} (${metadata.id})", exception)
        continue
      }

      addons.add(addonMetadata to addonInstance)
    }
  }

}
