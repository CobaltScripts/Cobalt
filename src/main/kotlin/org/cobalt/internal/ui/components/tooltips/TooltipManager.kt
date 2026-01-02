package org.cobalt.internal.ui.components.tooltips

import org.cobalt.internal.ui.UIComponent

internal object TooltipManager {
  private val tooltips = mutableListOf<UIComponent>()

  fun register(tooltip: UIComponent) {
    tooltips.add(tooltip)
  }

  fun renderAll() {
    tooltips.forEach { it.render() }
  }
}
