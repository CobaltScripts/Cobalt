package org.cobalt.internal.ui.components

import java.awt.Color
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.ui.UIComponent
import org.cobalt.internal.ui.animation.ColorAnimation
import org.cobalt.internal.ui.panel.panels.UIAddonList
import org.cobalt.internal.ui.screen.UIConfig
import org.cobalt.internal.ui.util.isHoveringOver

internal class UIBackButton : UIComponent(
  x = 0F,
  y = 0F,
  width = 30F,
  height = 30F
) {

  private val colorAnim = ColorAnimation(150L)
  private var wasHovering = false

  override fun render() {
    val hovering = isHoveringOver(x, y, width, height)

    if (hovering != wasHovering) {
      colorAnim.start()
      wasHovering = hovering
    }

    val bgColor = colorAnim.get(
      Color(42, 42, 42, 50),
      Color(61, 94, 149, 50),
      !hovering
    )

    val borderColor = colorAnim.get(
      Color(42, 42, 42),
      Color(61, 94, 149),
      !hovering
    )

    val arrowColor = colorAnim.get(
      Color(230, 230, 230),

      Color(61, 94, 149),

      !hovering
    )

    NVGRenderer.rect(x, y, width, height, bgColor.rgb, 5F)
    NVGRenderer.hollowRect(x, y, width, height, 2F, borderColor.rgb, 5F)
    NVGRenderer.image(
      leftArrow,
      x + width / 2F - 10F,
      y + height / 2F - 10F,
      20F, 20F, 0F,
      arrowColor.rgb
    )
  }

  override fun mouseClicked(button: Int): Boolean {
    if (isHoveringOver(x, y, width, height) && button == 0) {
      UIConfig.swapBodyPanel(UIAddonList())
    }

    return false
  }

  companion object {
    private val leftArrow = NVGRenderer.createImage("/assets/cobalt/icons/arrow-left.svg")
  }

}
