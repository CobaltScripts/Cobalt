package org.cobalt.pathfinder

import java.awt.Color
import org.cobalt.module.impl.misc.Debug
import org.cobalt.ui.theme.Theme
import org.cobalt.ui.theme.ThemeManager
import org.cobalt.util.client.PlayerUtils
import org.cobalt.util.render.GizmoRenderer

object PathRenderer {

  private inline val theme: Theme
    get() = ThemeManager.activeTheme

  fun render() {
    val path = PathExecutor.path ?: return
    val nodes = path.nodes

    val playerPos = PlayerUtils.blockStandingOn
    val targetNode = nodes[PathExecutor.pathIndex].block

    if (Debug.enabled) {
      GizmoRenderer.drawBlockPos(playerPos, Color.GREEN)
      GizmoRenderer.drawBlockPos(targetNode, Color.RED)
    }

    for (node in nodes) {
      val block = node.block

      if (Debug.enabled && (block == playerPos || block == targetNode)) {
        continue
      }

      GizmoRenderer.drawBlockPos(block, theme.accentSecondary)
    }
  }

}
