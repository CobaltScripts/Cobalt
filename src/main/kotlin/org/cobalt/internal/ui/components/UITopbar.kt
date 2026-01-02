package org.cobalt.internal.ui.components

import java.awt.Color
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.ui.UIComponent

internal class UITopbar(
  private var title: String,
) : UIComponent(
  x = 0F,
  y = 0F,
  width = 890F,
  height = 70F,
) {

  private val searchBar = UISearchBar()

  override fun render() {
    NVGRenderer.text(title, x + 40F, y + (height / 2) - 10F, 20F, Color(230, 230, 230).rgb)
    NVGRenderer.line(x, y + height, x + width, y + height, 1F, Color(42, 42, 42).rgb)

    searchBar
      .updateBounds(x, y)
      .render()
  }

  private class UISearchBar : UIComponent(
    x = 0F,
    y = 0F,
    width = 0F,
    height = 0F,
  ) {

    override fun render() {

    }

  }

}
