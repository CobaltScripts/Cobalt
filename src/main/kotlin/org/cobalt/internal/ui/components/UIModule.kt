package org.cobalt.internal.ui.components

import java.awt.Color
import kotlinx.coroutines.selects.select
import org.cobalt.api.module.Module
import org.cobalt.api.module.setting.Setting
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.ui.UIComponent
import org.cobalt.internal.ui.panel.panels.UIModuleList
import org.cobalt.internal.ui.util.isHoveringOver
import org.cobalt.internal.ui.animation.ColorAnimation
import org.cobalt.internal.ui.animation.EaseOutAnimation

internal class UIModule(
  private val module: Module,
  private val panel: UIModuleList,
  private var selected: Boolean,
) : UIComponent(
  x = 0F,
  y = 0F,
  width = 182.5F,
  height = 40F,
) {

  private val colorAnimation = ColorAnimation(150L)
  private val xOffsetAnimation = EaseOutAnimation(200L)

  override fun render() {
    val opaqueColor = colorAnimation.get(Color(0, 0, 0, 0), Color(61, 94, 149, 50), !selected)
    val mainColor = colorAnimation.get(Color(0, 0, 0, 0), Color(61, 94, 149), !selected)
    val textColor = colorAnimation.get(Color(230, 230, 230), Color(61, 94, 149), !selected)
    val xOffset = xOffsetAnimation.get(0F, 10F, !selected)

    if (selected) {
      NVGRenderer.rect(x, y, width, height, opaqueColor.rgb, 5F)
      NVGRenderer.hollowRect(x, y, width, height, 1F, mainColor.rgb, 5F)
      NVGRenderer.image(
        selectedIcon, x + 10F, y + height / 2 - 7F, 13F, 13F,
        colorMask = mainColor.rgb
      )
    }

    NVGRenderer.text(
      module.name, x + 20F + xOffset, y + height / 2F - 6.5F, 13F,
      textColor.rgb
    )
  }

  override fun mouseClicked(button: Int): Boolean {
    if (isHoveringOver(x, y, width, height) && button == 0) {
      panel.setModule(this)
      return true
    }

    return false
  }

  fun setSelected(selected: Boolean = true) {
    if (this.selected != selected) {
      this.selected = selected
      colorAnimation.start()
      xOffsetAnimation.start()
    }
  }

  fun getSettings(): List<Setting<*>> {
    return module.getSettings()
  }

  companion object {
    private val selectedIcon = NVGRenderer.createImage("/assets/cobalt/icons/selected.svg")
  }

}
