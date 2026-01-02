package org.cobalt.internal.ui.util

import kotlin.math.ceil
import org.cobalt.internal.ui.UIComponent

internal class GridLayout(
  private val columns: Int,
  private val itemWidth: Float,
  private val itemHeight: Float,
  private val gap: Float,
) {

  fun contentHeight(itemCount: Int): Float {
    val rows = ceil(itemCount.toFloat() / columns).toInt()
    return rows * (itemHeight + gap)
  }

  fun layout(
    startX: Float,
    startY: Float,
    components: List<UIComponent>
  ) {
    components.forEachIndexed { index, component ->
      val col = index % columns
      val row = index / columns

      val x = startX + col * (itemWidth + gap)
      val y = startY + row * (itemHeight + gap)

      component.updateBounds(x, y)
    }
  }

}

