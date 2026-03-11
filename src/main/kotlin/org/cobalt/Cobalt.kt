package org.cobalt

import net.fabricmc.api.ClientModInitializer
import org.cobalt.api.command.CommandManager
import org.cobalt.api.event.EventBus
import org.cobalt.api.hud.HudModuleManager
import org.cobalt.api.hud.modules.WatermarkModule
import org.cobalt.api.module.ModuleManager
import org.cobalt.api.notification.NotificationManager
import org.cobalt.api.rotation.RotationExecutor
import org.cobalt.api.util.TickScheduler
import org.cobalt.internal.command.MainCommand
import org.cobalt.internal.helper.Config
import org.cobalt.internal.loader.AddonLoader

@Suppress("UNUSED")
object Cobalt : ClientModInitializer {


  override fun onInitializeClient() {
    ModuleManager.addModules(listOf(WatermarkModule(), WatermarkModule()))

    AddonLoader.getAddons().map { it.second }.forEach {
      it.onLoad()
      ModuleManager.addModules(it.getModules())
    }

    CommandManager.register(MainCommand)
    CommandManager.dispatchAll()

    listOf(
      TickScheduler, MainCommand, NotificationManager,
      RotationExecutor, HudModuleManager,
    ).forEach { EventBus.register(it) }
    Config.loadModulesConfig()
    EventBus.register(this)
    println("Cobalt Mod Initialized")
  }

}
