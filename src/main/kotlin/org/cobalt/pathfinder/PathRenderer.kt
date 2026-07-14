package org.cobalt.pathfinder

import java.awt.Color
import org.cobalt.module.impl.misc.Debug
import org.cobalt.pathfinder.calculate.Path
import org.cobalt.ui.theme.Theme
import org.cobalt.ui.theme.ThemeManager
import org.cobalt.util.block.centerVec
import org.cobalt.util.block.smallBox
import org.cobalt.util.client.PlayerUtils
import org.cobalt.util.render.GizmoRenderer

object PathRenderer {

  private inline val theme: Theme
    get() = ThemeManager.activeTheme

  fun render() {
    val path: Path = PathExecutor.path ?: return

    val nodes = path.nodes
    val index = PathExecutor.pathIndex

    val targetNode = nodes[index].centerVec
    val playerPos = PlayerUtils.position.centerVec()

    if (Debug.enabled) {
      GizmoRenderer.drawBox(playerPos.smallBox(), Color.GREEN)
      GizmoRenderer.drawBox(targetNode.smallBox(), Color.RED)
    }

    for (index in 1 until nodes.size) {
      val prev = nodes[index - 1]
      val curr = nodes[index]

      GizmoRenderer.drawLine(
        prev.centerVec,
        curr.centerVec,
        theme.accentSecondary
      )
    }
  }

}
