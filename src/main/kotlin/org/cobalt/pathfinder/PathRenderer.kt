package org.cobalt.pathfinder

import org.cobalt.dsl.centerVec
import org.cobalt.dsl.smallBox
import org.cobalt.module.impl.misc.Debug
import org.cobalt.ui.theme.ThemeManager
import org.cobalt.util.PlayerUtils
import org.cobalt.util.WorldRenderUtils
import java.awt.Color
import org.cobalt.pathfinder.calculate.Path
import org.cobalt.ui.theme.Theme

object PathRenderer {

  private inline val theme: Theme
    get() = ThemeManager.activeTheme

  fun render() {
    val path: Path = PathExecutor.path ?: return

    val nodes = path.nodes
    val keyNodes = path.keyNodes

    val targetNode = nodes[PathExecutor.pathIndex].centerVec
    val playerPos = PlayerUtils.position.centerVec()

    if (Debug.enabled) {
      WorldRenderUtils.drawBox(playerPos.smallBox(), Color.GREEN)
      WorldRenderUtils.drawBox(targetNode.smallBox(), Color.RED)
    }

    for (index in keyNodes.indices) {
      val node = keyNodes[index]

      WorldRenderUtils.drawBlockPos(
        if (node.useMovementFly) node.block else node.blockStandingOn,
        color = theme.accentPrimary
      )

      if (index <= 0) continue

      val prev = keyNodes[index - 1]

      WorldRenderUtils.drawLine(
        if (prev.useMovementFly) prev.centerVec else prev.topCenterVec,
        if (node.useMovementFly) node.centerVec else node.topCenterVec,
        theme.accentSecondary
      )
    }
  }

}
