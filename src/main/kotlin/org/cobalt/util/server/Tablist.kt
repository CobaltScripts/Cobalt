package org.cobalt.util.server

import net.minecraft.network.chat.Component
import net.minecraft.world.level.GameType
import org.cobalt.Cobalt.minecraft

object Tablist {

  @JvmStatic
  val lines: List<Component>
    get() {
      val connection = minecraft.connection ?: return emptyList()

      return connection.listedOnlinePlayers
        .sortedWith(
          compareBy(
            { it.gameMode == GameType.SPECTATOR },
            { it.team?.name.orEmpty() },
            { it.profile.name }
          )
        )
        .map { it.tabListDisplayName ?: Component.literal(it.profile.name) }
    }

}
