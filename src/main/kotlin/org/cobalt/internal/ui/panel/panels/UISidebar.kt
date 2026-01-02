package org.cobalt.internal.ui.panel.panels

import java.awt.Color
import kotlin.random.Random
import kotlin.random.nextInt
import net.minecraft.client.MinecraftClient
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.ui.UIComponent
import org.cobalt.internal.ui.components.tooltips.TooltipPosition
import org.cobalt.internal.ui.components.tooltips.UITextTooltip
import org.cobalt.internal.ui.components.tooltips.UITooltip
import org.cobalt.internal.ui.panel.UIPanel
import org.cobalt.internal.ui.screen.UIConfig
import org.cobalt.internal.ui.util.isHoveringOver

internal class UISidebar : UIPanel(
  x = 0F,
  y = 0F,
  width = 70F,
  height = 600F
) {

  private val moduleButton = UIButton("/assets/cobalt/icons/box.svg") {
    UIConfig.swapBodyPanel(UIAddonList())
  }

//  private val hudButton = UIButton("/assets/cobalt/icons/interface.svg") {
//    println("Opening HUD Editor")
//  }

  private val steveIcon = NVGRenderer.createImage("/assets/cobalt/steve.png")
  private val userIcon = MinecraftClient.getInstance().session.uuidOrNull?.let {
    try {
      NVGRenderer.createImage(
        "https://mc-heads.net/avatar/${MinecraftClient.getInstance().session.uuidOrNull}/100/face.png"
      )
    } catch (_: Exception) {
      steveIcon
    }
  } ?: steveIcon

  private val userIconTooltip = UITooltip(
    content = { UITextTooltip("Hello, ${MinecraftClient.getInstance().session.username}!") },
    position = TooltipPosition.BELOW
  )

  init {
    components.addAll(
      listOf(moduleButton)
    )
  }

  override fun render() {
    NVGRenderer.rect(x, y, width, height, Color(18, 18, 18).rgb, 10F)
    val hue = Random.nextDouble(0.0, 1.0).toFloat()
    val neon = Color.getHSBColor(hue, 0.85f + Random.nextFloat() * 0.15f, 0.9f + Random.nextFloat() * 0.1f)
    NVGRenderer.text("cb", x + width / 2F - 15F, y + 25F, 25F, neon.rgb)
    moduleButton
      .setSelected(true)
      .updateBounds(x + (width / 2F) - (moduleButton.width / 2F), y + 75F)
      .render()

//    hudButton
//      .updateBounds(x + (width / 2F) - (hudButton.width / 2F), y + 115F)
//      .render()

    val userIconX = x + (width / 2F) - 16F
    val userIconY = y + height - 32F - 20F

    NVGRenderer.image(
      userIcon,
      userIconX,
      userIconY,
      32F,
      32F,
      radius = 10F
    )

    userIconTooltip.updateBounds(userIconX, userIconY, 32F, 32F)
  }

  private class UIButton(
    iconPath: String,
    private val onClick: () -> Unit
  ) : UIComponent(0f, 0f, 22F, 22F) {

    val image = NVGRenderer.createImage(iconPath)
    private var selected = false

    fun setSelected(selected: Boolean): UIComponent {
      this.selected = selected
      return this
    }

    override fun render() {
      val hovering = isHoveringOver(x, y, width, height)

      NVGRenderer.image(
        image,
        x, y, width, height,

        colorMask = if (hovering || selected)
          Color(61, 94, 149).rgb
        else
          Color(120, 120, 120).rgb
      )
    }

    override fun mouseClicked(button: Int): Boolean {
      if (isHoveringOver(x, y, width, height) && button == 0) {
        onClick.invoke()
        return true
      }

      return false
    }

  }

}
