package org.cobalt.ui.component

import org.cobalt.module.Module
import org.cobalt.ui.UIComponent
import org.cobalt.ui.animation.EaseOutAnimation
import org.cobalt.util.render.SkiaRenderer

class ModuleComponent(
  val module: Module,
) : UIComponent(
  width = WIDTH,
  height = BASE_HEIGHT
) {

  private val settings = module.getSettings()
  private val switch: UIComponent? = if (module.toggleable) SwitchComponent(module) else null
  private val expandAnimation = EaseOutAnimation(150L)
  private var lastEnabledState = module.enabled

  private val expandedSettingsHeight: Float
    get() = settings.fold(0f) { acc, setting -> acc + setting.height } +
      if (settings.isNotEmpty()) 11f else 0f

  private val expandProgress: Float
    get() {
      if (!module.toggleable) {
        return 1f
      }

      return expandAnimation.get(
        if (module.enabled) 0f else 1f,
        if (module.enabled) 1f else 0f,
        false
      )
    }

  override val height: Float
    get() = BASE_HEIGHT + expandedSettingsHeight * expandProgress

  init {
    switch?.let(::addChild)
    settings.forEach(::addChild)
  }

  override fun renderComponent() {
    SkiaRenderer.roundedRect(
      x = xPos,
      y = yPos,
      width = width,
      height = height,
      radius = 5f,
      color = theme.backgroundSecondary
    )

    switch?.let { switch ->
      val switchX = xPos + width - switch.width - PADDING
      val switchY = yPos + (BASE_HEIGHT - switch.height) / 2

      switch
        .updateBounds(switchX, switchY)
        .renderComponent()
    }

    if (settings.isNotEmpty() && expandProgress > 0f) {
      SkiaRenderer.line(
        x1 = xPos + PADDING,
        y1 = yPos + BASE_HEIGHT,
        x2 = xPos + width - PADDING,
        y2 = yPos + BASE_HEIGHT,
        thickness = 1f,
        color = theme.border
      )

      SkiaRenderer.pushScissor(
        x = xPos,
        y = yPos + BASE_HEIGHT,
        width = width,
        height = expandedSettingsHeight * expandProgress
      )

      var settingY = yPos + BASE_HEIGHT + 6f

      settings.forEach { setting ->
        setting
          .updateBounds(xPos, settingY)
          .renderComponent()
        settingY += setting.height
      }

      SkiaRenderer.popScissor()
    }

    SkiaRenderer.roundedOutline(
      x = xPos,
      y = yPos,
      width = width,
      height = height,
      thickness = 1f,
      radius = 5f,
      color = theme.border
    )

    SkiaRenderer.text(
      font = SkiaRenderer.regularFont,
      text = module.name,
      x = xPos + PADDING,
      y = yPos + (BASE_HEIGHT - FONT_SIZE) / 2,
      size = FONT_SIZE,
      color = theme.textPrimary
    )
  }

  override fun mouseClicked(button: Int): Boolean {
    val consumed = super.mouseClicked(button)

    if (module.toggleable && consumed && lastEnabledState != module.enabled) {
      expandAnimation.start()
      lastEnabledState = module.enabled
    }

    return consumed
  }

  companion object {
    val WIDTH = (TopbarComponent.width - 60) / 2f

    private const val BASE_HEIGHT = 60f
    private const val FONT_SIZE = 16f
    private const val PADDING = 20f
  }

}
