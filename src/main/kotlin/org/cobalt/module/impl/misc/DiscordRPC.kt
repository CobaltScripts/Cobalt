package org.cobalt.module.impl.misc

import com.jagrosh.discordipc.IPCClient
import com.jagrosh.discordipc.entities.ActivityType
import com.jagrosh.discordipc.entities.RichPresence
import com.jagrosh.discordipc.entities.pipe.PipeStatus
import org.cobalt.Cobalt
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.TickEvent
import org.cobalt.module.Module
import org.cobalt.module.ModuleCategory
import org.slf4j.LoggerFactory

object DiscordRPC : Module(
  name = "DiscordRPC",
  category = ModuleCategory.MISC,
) {

  private val logger = LoggerFactory.getLogger(this::class.java)

  private var client: IPCClient? = null
  private var lastUpdate = 0L
  private var doNotTryAgain = false

  private const val APPLICATION_ID = 1441864552936636519L

  override fun onEnable() {
    super.onEnable()
    doNotTryAgain = false
  }

  @SubscribeEvent
  fun onTick(ignored: TickEvent.Start) {
    if (enabled) {
      connectIpc()
    } else {
      disconnectIpc()
    }

    val client = client

    if (client == null || client.status != PipeStatus.CONNECTED) {
      return
    }

    if (System.currentTimeMillis() - lastUpdate < 1800000) {
      return
    }

    client.sendRichPresence(buildRichPresence())
    lastUpdate = System.currentTimeMillis()
  }

  private fun connectIpc() {
    if (doNotTryAgain || client?.status == PipeStatus.CONNECTED) {
      return
    }

    runCatching {
      client = IPCClient(APPLICATION_ID)
      client?.connect()
    }.onFailure {
      logger.error("Failed to connect to Discord RPC.", it)
      doNotTryAgain = true
    }
  }

  private fun disconnectIpc() {
    val client = client

    if (client == null || client.status != PipeStatus.CONNECTED) {
      return
    }

    client.close()
  }

  private fun buildRichPresence(): RichPresence {
    val rpc = RichPresence.Builder()
    val minecraftVersion = "Minecraft ${Cobalt.MINECRAFT_VERSION}"

    rpc.setActivityType(ActivityType.Playing)
    rpc.setLargeImage("logo", "${Cobalt.MOD_NAME} ${Cobalt.MOD_VERSION}", null)
    rpc.setSmallImage("minecraft", minecraftVersion, null)
    rpc.setDetails("Playing $minecraftVersion")
    rpc.setStartTimestamp(System.currentTimeMillis())
    rpc.setState(states.random())

    return rpc.build()
  }

  private val states = listOf(
    "Arguing With Jerry",
    "Selling Dirt",
    "Not Macroing (Trust)",
    "Fell Into the Void",
    "Waiting for Diana",
    "Mining Cobble",
    "Talking to Bank Guard",
    "Broke Again",
    "AFK But Not Really",
    "Forgot Arrows",
    "Reorganizing Chests",
    "Selling My Minions",
    "Questioning Life",
    "Lagging in the Hub",
    "Flexing Nonexistent Necron",
    "Staring at Minions",
    "Lost 10M to Taxes",
    "Fishing Seaweed",
    "Mining Dirt Passionately",
    "Touching Grass (Rare)"
  )

}
