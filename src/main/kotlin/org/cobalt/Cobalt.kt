package org.cobalt

import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.MinecraftClient
import org.cobalt.api.command.CommandManager
import org.cobalt.api.event.EventBus
import org.cobalt.api.event.annotation.SubscribeEvent
import org.cobalt.api.event.impl.client.TickEvent
import org.cobalt.api.event.impl.render.WorldRenderEvent
import org.cobalt.api.module.ModuleManager
import org.cobalt.api.notification.NotificationManager
import org.cobalt.api.pathfinder.IPathExec
import org.cobalt.api.util.TickScheduler
import org.cobalt.api.util.rotation.IRotationExec
import org.cobalt.internal.command.MainCommand
import org.cobalt.internal.helper.Config
import org.cobalt.internal.loader.AddonLoader
import org.cobalt.internal.modules.DiscordPresence
import org.cobalt.internal.rotation.RotationExec

object Cobalt : ClientModInitializer {

  const val MOD_NAME = "Cobalt"
  const val VERSION = "1.0.0"

  val MC_VERSION: String
    get() = mc.gameVersion

  val mc: MinecraftClient
    get() = MinecraftClient.getInstance()

  @Suppress("UNUSED_EXPRESSION")
  override fun onInitializeClient() {
    AddonLoader.getAddons().map { it.second }.forEach {
      it.onLoad()
      ModuleManager.addModules(it.getModules())
    }

    CommandManager.register(MainCommand)
    CommandManager.dispatchAll()

    listOf(TickScheduler, DiscordPresence, MainCommand, NotificationManager).forEach { EventBus.register(it) }

    Config.loadModulesConfig()
    EventBus.register(this)
    println("Cobalt Mod Initialized")
  }

  @JvmStatic private var rotationExec: IRotationExec = RotationExec

  @JvmStatic private var pathExec: IPathExec? = null

  @JvmStatic fun getRotationExec() = rotationExec

  @JvmStatic fun getPathExec() = pathExec

  @JvmStatic
  fun setPathExec(pathExec: IPathExec) {
    this.pathExec = pathExec
  }

  @JvmStatic
  fun setRotationExec(rotationExec: IRotationExec) {
    this.rotationExec = rotationExec
  }

  @SubscribeEvent
  fun onTick(event: TickEvent.End) {
    mc.player?.let { pathExec?.onTick(it) }
  }

  @SubscribeEvent
  fun onWorldRenderLast(event: WorldRenderEvent.Last) {
    mc.player?.let { rotationExec.onRotate(it) }
    mc.player?.let { pathExec?.onWorldRenderLast(event.context, it) }
  }
}
