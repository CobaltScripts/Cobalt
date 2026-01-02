package org.cobalt.internal.ui.panel.panels

import java.awt.Color
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.loader.AddonLoader
import org.cobalt.internal.ui.UIComponent
import org.cobalt.internal.ui.components.UIAddonEntry
import org.cobalt.internal.ui.components.UITopbar
import org.cobalt.internal.ui.panel.UIPanel
import org.cobalt.internal.ui.util.GridLayout
import org.cobalt.internal.ui.util.ScrollHandler
import org.cobalt.internal.ui.util.isHoveringOver

internal class UIAddonList : UIPanel(
  x = 0F,
  y = 0F,
  width = 890F,
  height = 600F
) {

  private val topBar = UITopbar("Addons")
  private val entries = AddonLoader.getAddons().map { UIAddonEntry(it.first, it.second) }

  private val gridLayout = GridLayout(
    columns = 3,
    itemWidth = 270F,
    itemHeight = 70F,
    gap = 20F
  )

  private val scrollHandler = ScrollHandler()

  init {
    components.addAll(entries)
    components.add(topBar)
  }

  override fun render() {
    NVGRenderer.rect(x, y, width, height, Color(18, 18, 18).rgb, 10F)

    topBar
      .updateBounds(x, y)
      .render()

    val startY = y + topBar.height
    val visibleHeight = height - topBar.height

    scrollHandler.setMaxScroll(gridLayout.contentHeight(entries.size) + 20F, visibleHeight)
    NVGRenderer.pushScissor(x, startY, width, visibleHeight)

    val scrollOffset = scrollHandler.getOffset()
    gridLayout.layout(x + 20F, startY + 20F - scrollOffset, entries)
    entries.forEach(UIComponent::render)

    NVGRenderer.popScissor()
  }

  override fun mouseScrolled(horizontalAmount: Double, verticalAmount: Double): Boolean {
    if (isHoveringOver(x, y, width, height)) {
      scrollHandler.handleScroll(verticalAmount)
      return true
    }

    return false
  }

}
