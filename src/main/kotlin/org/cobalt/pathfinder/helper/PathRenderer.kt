package org.cobalt.pathfinder.helper

import java.awt.Color
import org.cobalt.dsl.centerVec
import org.cobalt.dsl.smallBox
import org.cobalt.module.impl.misc.Debug
import org.cobalt.pathfinder.PathFindingFacade.path
import org.cobalt.pathfinder.PathFindingFacade.pathIndex
import org.cobalt.ui.theme.ThemeManager
import org.cobalt.util.PlayerUtils
import org.cobalt.util.WorldRenderUtils

object PathRenderer {
  fun render() {
    val theme = ThemeManager.activeTheme
    val nodes = path?.nodes ?: return
    val keyNodes = path?.keyNodes ?: return

    val targetNode = nodes[pathIndex].centerVec
    val playerPos = PlayerUtils.position.centerVec()

    if (Debug.enabled) {
      WorldRenderUtils.drawBox(playerPos.smallBox(), Color.GREEN)
      WorldRenderUtils.drawBox(targetNode.smallBox(), Color.RED)
    }

    for (index in keyNodes.indices) {
      val node = keyNodes[index]

      WorldRenderUtils.drawBlockPos(
        if (node.isFly) node.block else node.blockStandingOn,
        color = theme.accentPrimary
      )

      if (index <= 0) continue

      val prev = keyNodes[index - 1]

      WorldRenderUtils.drawLine(
        if (prev.isFly) prev.centerVec else prev.topCenterVec,
        if (node.isFly) node.centerVec else node.topCenterVec,
        theme.accentSecondary
      )
    }
  }
}
