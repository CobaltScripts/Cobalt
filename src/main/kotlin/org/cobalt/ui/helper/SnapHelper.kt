package org.cobalt.ui.helper

import kotlin.math.abs
import org.cobalt.util.client.WindowUtils

class SnapHelper(private val snapThreshold: Float = 5f) {

  var activeGuides: List<GuideLine> = emptyList()
    private set

  fun findAlignmentGuides(
    moduleX: Float,
    moduleY: Float,
    moduleW: Float,
    moduleH: Float,
    otherModuleBounds: List<ModuleBounds>,
  ): Pair<Float, Float> {
    val right = moduleX + moduleW
    val centerX = moduleX + moduleW / 2f
    val bottom = moduleY + moduleH
    val centerY = moduleY + moduleH / 2f

    val xTargets = mutableListOf(0f, WindowUtils.windowWidth / 2f, WindowUtils.windowWidth)
    val yTargets = mutableListOf(0f, WindowUtils.windowHeight / 2f, WindowUtils.windowHeight)

    otherModuleBounds.forEach { bounds ->
      xTargets.add(bounds.x)
      xTargets.add(bounds.x + bounds.w)
      xTargets.add(bounds.x + bounds.w / 2f)
      yTargets.add(bounds.y)
      yTargets.add(bounds.y + bounds.h)
      yTargets.add(bounds.y + bounds.h / 2f)
    }

    val xEdges = listOf(moduleX to 0f, centerX to moduleW / 2f, right to moduleW)
    val yEdges = listOf(moduleY to 0f, centerY to moduleH / 2f, bottom to moduleH)

    val xMatch = findClosestSnap(xTargets, xEdges)
    val yMatch = findClosestSnap(yTargets, yEdges)

    activeGuides = listOfNotNull(
      xMatch?.let { GuideLine(isVertical = true, position = it.target) },
      yMatch?.let { GuideLine(isVertical = false, position = it.target) },
    )

    return (xMatch?.snappedOrigin ?: moduleX) to (yMatch?.snappedOrigin ?: moduleY)
  }

  fun clearGuides() {
    activeGuides = emptyList()
  }

  private fun findClosestSnap(targets: List<Float>, edges: List<Pair<Float, Float>>): SnapMatch? {
    var best: SnapMatch? = null
    for (target in targets) {
      for ((edgePosition, offset) in edges) {
        val diff = abs(edgePosition - target)
        if (diff <= snapThreshold && (best == null || diff < best.diff)) {
          best = SnapMatch(diff = diff, target = target, snappedOrigin = target - offset)
        }
      }
    }
    return best
  }

  private data class SnapMatch(val diff: Float, val target: Float, val snappedOrigin: Float)

  data class GuideLine(val isVertical: Boolean, val position: Float)
  data class ModuleBounds(val x: Float, val y: Float, val w: Float, val h: Float)
}
