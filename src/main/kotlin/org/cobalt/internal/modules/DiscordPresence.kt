package org.cobalt.internal.modules

import meteordevelopment.discordipc.DiscordIPC
import meteordevelopment.discordipc.RichPresence
import org.cobalt.Cobalt
import org.cobalt.api.event.annotation.SubscribeEvent
import org.cobalt.api.event.impl.client.TickEvent

// TODO: Move to QOL Addon
object DiscordPresence {

  private val rpc: RichPresence = RichPresence()
  private var lastUpdate: Long = System.currentTimeMillis()

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

  init {
    DiscordIPC.start(1406359679772266608L, null)

    rpc.setStart(System.currentTimeMillis() / 1000L)
    rpc.setLargeImage("logo", "${Cobalt.MOD_NAME} ${Cobalt.VERSION}")
    rpc.setDetails("Minecraft ${Cobalt.MC_VERSION}")
    rpc.setState(states.random())

    DiscordIPC.setActivity(rpc)
  }

  @SubscribeEvent
  fun onTick(ignored: TickEvent.End) {
    if (System.currentTimeMillis() - lastUpdate < 1_800_000)
      return

    rpc.setState(states.random())
    DiscordIPC.setActivity(rpc)
    lastUpdate = System.currentTimeMillis()
  }

}
