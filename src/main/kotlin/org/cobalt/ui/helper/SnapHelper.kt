package org.cobalt.ui.helper

import kotlin.math.abs
import org.cobalt.util.WindowUtils

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

    var snappedX = moduleX
    var snappedY = moduleY
    val bestXDiff = snapThreshold + 1f
    val bestYDiff = snapThreshold + 1f
    var xGuide: GuideLine? = null
    var yGuide: GuideLine? = null

    fun checkX(target: Float, edge: Float, newX: Float) {
      val diff = abs(edge - target)
      if (diff <= snapThreshold && diff < bestXDiff) {
        snappedX = newX
        xGuide = GuideLine(true, target)
      }
    }

    fun checkY(target: Float, edge: Float, newY: Float) {
      val diff = abs(edge - target)
      if (diff <= snapThreshold && diff < bestYDiff) {
        snappedY = newY
        yGuide = GuideLine(false, target)
      }
    }

    xTargets.forEach { target ->
      checkX(target, moduleX, target)
      checkX(target, centerX, target - moduleW / 2f)
      checkX(target, right, target - moduleW)
    }

    yTargets.forEach { target ->
      checkY(target, moduleY, target)
      checkY(target, centerY, target - moduleH / 2f)
      checkY(target, bottom, target - moduleH)
    }

    activeGuides = listOfNotNull(xGuide, yGuide)
    return snappedX to snappedY
  }

  fun clearGuides() {
    activeGuides = emptyList()
  }

  data class GuideLine(val isVertical: Boolean, val position: Float)
  data class ModuleBounds(val x: Float, val y: Float, val w: Float, val h: Float)

}
