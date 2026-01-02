package org.cobalt.internal.ui.components

import java.awt.Color
import org.cobalt.api.addon.Addon
import org.cobalt.api.addon.AddonMetadata
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.loader.AddonLoader
import org.cobalt.internal.ui.UIComponent
import org.cobalt.internal.ui.panel.panels.UIModuleList
import org.cobalt.internal.ui.screen.UIConfig
import org.cobalt.internal.ui.util.isHoveringOver

internal class UIAddonEntry(
  private val metadata: AddonMetadata,
  private val addon: Addon,
) : UIComponent(
  x = 0F,
  y = 0F,
  width = 270F,
  height = 70F,
) {
  val addonIcon = AddonLoader.getAddonIcon(metadata.id) ?: boxIcon

  override fun render() {
    NVGRenderer.rect(
      x, y, width, height,
      Color(24, 24, 24).rgb, 10F
    )

    NVGRenderer.hollowRect(
      x, y, width, height,
      1F, Color(42, 42, 42).rgb, 10F
    )

    NVGRenderer.rect(x + 10F, y + height / 2F - 25F, 50F, 50F, Color(30, 30, 30).rgb, 5F)

    NVGRenderer.image(
      addonIcon, x + 20F, y + height / 2F - 15F, 30F, 30F,
      colorMask = Color(200, 200, 200).rgb
    )

    NVGRenderer.text(
      metadata.name,
      x + 75F,
      y + (height - 29F) / 2F,
      14F,
      Color(230, 230, 230).rgb
    )

    NVGRenderer.text(
      "v${metadata.version}",
      x + 75F,
      y + (height - 29F) / 2F + 17F,
      12F,
      Color(179, 179, 179).rgb
    )
  }

  override fun mouseClicked(button: Int): Boolean {
    if (isHoveringOver(x, y, width, height) && button == 1) {
      UIConfig.swapBodyPanel(UIModuleList(metadata, addon))
      return true
    }

    return false
  }

  companion object {

    private val boxIcon = NVGRenderer.createImage("/assets/cobalt/icons/box.svg")
  }

}
