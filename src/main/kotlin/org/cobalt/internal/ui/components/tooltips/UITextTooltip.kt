package org.cobalt.internal.ui.components.tooltips

import java.awt.Color
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.ui.UIComponent

internal class UITextTooltip(
  private val text: String,
  private val textHeight: Float = 16F
) : UIComponent(0f, 0f, 0f, 0f) {

  private val padding = 8F

  init {
    width = NVGRenderer.textWidth(text, textHeight) + (padding * 2)
    height = textHeight + (padding * 2)
  }

  override fun render() {
    NVGRenderer.text(
      text,
      x + padding,
      y + padding + 3F,
      textHeight,
      Color(230, 230, 230).rgb
    )
  }
}
