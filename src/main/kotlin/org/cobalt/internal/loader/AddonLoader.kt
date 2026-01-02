package org.cobalt.internal.loader

import com.google.gson.Gson
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipFile
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import org.cobalt.api.addon.Addon
import org.cobalt.api.addon.AddonMetadata
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.api.util.ui.helper.Image
import org.cobalt.internal.test.TestAddon
import org.spongepowered.asm.mixin.Mixins

object AddonLoader {

  private val addonsDir: Path = Paths.get("config/cobalt/addons/")
  private val addons = mutableListOf<Pair<AddonMetadata, Addon>>()
  private val gson = Gson()


  fun findAddons() {
    //REMOVE TO REMOVE TEST ADDON!!!
    val testAddon = TestAddon()
    addons += AddonMetadata(
      id = "test",
      name = "Test",
      version = "1.0.0",
      entrypoints = listOf(),
      mixins = listOf(),
      icon = "https://i.imgur.com/UD3Lija.png"
    ) to testAddon

    if (FabricLauncherBase.getLauncher().isDevelopment) {
      for (entry in FabricLoader.getInstance().getEntrypointContainers("cobalt", Addon::class.java)) {
        val modMeta = entry.provider.metadata
        val metadata = AddonMetadata(
          id = modMeta.id,
          name   = modMeta.name,
          version = modMeta.version?.toString() ?: "unknown",
          entrypoints = listOf(entry.entrypoint.javaClass.name),
          mixins = listOf()
        )

        val addonInstance: Addon = try {
          entry.entrypoint
        } catch (e: Throwable) {
          throw RuntimeException("Failed to initialize addon \"${modMeta.name}\"", e)
        }

        addons += metadata to addonInstance
      }
    }

    if (!Files.isDirectory(addonsDir)) {
      Files.createDirectories(addonsDir)
      return
    }

    try {
      Files.newDirectoryStream(addonsDir, "*.jar").use { stream ->
        for (jarPath in stream) {
          FabricLauncherBase.getLauncher().addToClassPath(jarPath)
          loadAddon(jarPath)
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun loadAddon(jarPath: Path): AddonMetadata {
    ZipFile(jarPath.toFile()).use { zip ->
      val jsonEntry = zip.getEntry("cobalt.addon.json")
        ?: throw IllegalStateException("Missing cobalt.addon.json in $jarPath")

      val metadata = zip.getInputStream(jsonEntry).use { input ->
        gson.fromJson(input.reader(), AddonMetadata::class.java)
      }

      synchronized(Mixins::class.java) {
        for (mixin in metadata.mixins) {
          Mixins.addConfiguration(mixin)
        }
      }

      for (entrypoint in metadata.entrypoints) {
        val classPath = entrypoint.replace('.', '/') + ".class"

        if (zip.getEntry(classPath) == null) {
          throw IllegalStateException(
            "Entrypoint class '$entrypoint' does not exist inside ${jarPath.fileName}"
          )
        }

        val instance = Class.forName(entrypoint).let {
          try {
            it.getField("INSTANCE").get(null)
          } catch (_: NoSuchFieldException) {
            val constructor = it.getDeclaredConstructor()
            constructor.isAccessible = true
            constructor.newInstance()
          }
        }

        if (instance !is Addon) {
          throw IllegalStateException(
            "Entrypoint '$entrypoint' must implement Addon"
          )
        }

        addons += metadata to instance
      }

      return metadata
    }
  }

  fun getAddons(): List<Pair<AddonMetadata, Addon>> {
    return addons.toList()
  }

  fun getAddonIcon(addonId: String): Image? {
    return NVGRenderer.createImage(
      addons.find { it.first.id == addonId }?.first?.icon ?: return null
    )
  }

}

