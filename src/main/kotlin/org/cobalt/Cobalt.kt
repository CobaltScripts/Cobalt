package org.cobalt

import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.minecraft.SharedConstants
import net.minecraft.client.Minecraft
import org.cobalt.addon.AddonManager
import org.cobalt.command.CommandManager
import org.cobalt.util.failsafe.FailsafeManager
import org.cobalt.module.ModuleManager
import org.cobalt.ui.theme.ThemeManager
import org.cobalt.util.input.KeyMappingHandler
import org.cobalt.util.render.skia.SkiaPIP
import org.cobalt.util.web.UpdateChecker
import org.slf4j.LoggerFactory

object Cobalt : ClientModInitializer {

  @JvmStatic
  val minecraft: Minecraft
    get() = Minecraft.getInstance()

  @JvmStatic
  val configDir: Path
    get() = minecraft.gameDirectory.toPath()
      .resolve("config/cobalt")

  @JvmField
  val MOD_CONTAINER: ModContainer = FabricLoader.getInstance().getModContainer("cobalt").orElseThrow()

  @JvmField
  val MOD_ID: String = MOD_CONTAINER.metadata.id

  @JvmField
  val MOD_NAME: String = MOD_CONTAINER.metadata.name

  @JvmField
  val MOD_VERSION: String = MOD_CONTAINER.metadata.version.friendlyString

  @JvmField
  val MINECRAFT_VERSION: String = SharedConstants.getCurrentVersion().name()

  private val logger =
    LoggerFactory.getLogger(this::class.java)

  override fun onInitializeClient() {
    logger.info("Initializing $MOD_NAME $MINECRAFT_VERSION (v$MOD_VERSION)")

    ThemeManager.loadThemes()

    ModuleManager.registerModules()
    CommandManager.registerCommands()

    FailsafeManager.initialize()
    KeyMappingHandler.registerKeyMappings()

    CompletableFuture.runAsync {
      UpdateChecker.runCheck()
    }

    PictureInPictureRendererRegistry.register { SkiaPIP() }
  }

  @JvmStatic
  fun runOnClientThread(action: () -> Unit) {
    if (minecraft.isSameThread) {
      action()
    } else {
      minecraft.execute(action)
    }
  }

}
