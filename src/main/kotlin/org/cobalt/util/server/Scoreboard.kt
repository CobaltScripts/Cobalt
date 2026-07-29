package org.cobalt.util.server

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.numbers.StyledFormat
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.PlayerTeam
import org.cobalt.Cobalt.minecraft
import org.cobalt.mixin.gui.GuiAccessor

object Scoreboard {

  @JvmStatic
  val title: Component?
    get() {
      val scoreboard = minecraft.level?.scoreboard ?: return null
      val objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) ?: return null

      return objective.displayName
    }

  @JvmStatic
  val lines: List<Component>
    get() {
      val scoreboard = minecraft.level?.scoreboard ?: return emptyList()
      val objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) ?: return emptyList()
      val numberFormat = objective.numberFormatOrDefault(StyledFormat.SIDEBAR_DEFAULT)

      return scoreboard.listPlayerScores(objective)
        .asSequence()
        .filter { !it.isHidden }
        .sortedWith(GuiAccessor.`cobalt$getScoreDisplayOrder`())
        .take(15)
        .map { entry ->
          val team = scoreboard.getPlayersTeam(entry.owner())
          val name = entry.ownerName()

          val text = PlayerTeam.formatNameForTeam(team, name)
          val value = entry.formatValue(numberFormat)

          Component.empty()
            .append(text)
            .append(Component.literal(" "))
            .append(value)
        }
        .toList()
    }

}
