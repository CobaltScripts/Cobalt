package org.cobalt

import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.MinecraftClient
import org.cobalt.api.command.CommandManager
import org.cobalt.api.event.EventBus
import org.cobalt.internal.command.MainCommand
import org.cobalt.api.util.TickScheduler
import org.cobalt.internal.rpc.RichPresenceManager

object Cobalt : ClientModInitializer{
  val mc: MinecraftClient
    get() = MinecraftClient.getInstance()

  @Suppress("UNUSED_EXPRESSION")
  override fun onInitializeClient() {
    CommandManager.register(MainCommand)
    CommandManager.dispatchAll()

    listOf(
      TickScheduler
    ).forEach { EventBus.register(it) }

    RichPresenceManager.startRpc()
    println("Cobalt Mod Initialized")
  }
  
}
