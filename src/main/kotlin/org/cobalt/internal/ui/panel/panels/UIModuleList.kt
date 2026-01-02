package org.cobalt.internal.ui.panel.panels

import java.awt.Color
import org.cobalt.api.addon.Addon
import org.cobalt.api.addon.AddonMetadata
import org.cobalt.api.module.setting.impl.CheckboxSetting
import org.cobalt.api.module.setting.impl.ColorSetting
import org.cobalt.api.module.setting.impl.InfoSetting
import org.cobalt.api.module.setting.impl.KeyBindSetting
import org.cobalt.api.module.setting.impl.ModeSetting
import org.cobalt.api.module.setting.impl.RangeSetting
import org.cobalt.api.module.setting.impl.SliderSetting
import org.cobalt.api.module.setting.impl.TextSetting
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.loader.AddonLoader
import org.cobalt.internal.ui.UIComponent
import org.cobalt.internal.ui.components.UIBackButton
import org.cobalt.internal.ui.components.UIModule
import org.cobalt.internal.ui.components.UITopbar
import org.cobalt.internal.ui.components.settings.UICheckboxSetting
import org.cobalt.internal.ui.components.settings.UIColorSetting
import org.cobalt.internal.ui.components.settings.UIInfoSetting
import org.cobalt.internal.ui.components.settings.UIKeyBindSetting
import org.cobalt.internal.ui.components.settings.UIModeSetting
import org.cobalt.internal.ui.components.settings.UIRangeSetting
import org.cobalt.internal.ui.components.settings.UISliderSetting
import org.cobalt.internal.ui.components.settings.UITextSetting
import org.cobalt.internal.ui.panel.UIPanel
import org.cobalt.internal.ui.util.GridLayout
import org.cobalt.internal.ui.util.ScrollHandler
import org.cobalt.internal.ui.util.isHoveringOver

internal class UIModuleList(
  private val metadata: AddonMetadata,
  addon: Addon,
) : UIPanel(
  x = 0F,
  y = 0F,
  width = 890F,
  height = 600F
) {

  private val topBar = UITopbar("Modules")
  private val backButton = UIBackButton()

  /**
   * Modules list (left side)
   */
  private val modules = addon.getModules()
    .withIndex()
    .map { (index, module) ->
      UIModule(module, this, index == 0)
    }

  private val modulesScroll = ScrollHandler()
  private val modulesLayout = GridLayout(
    columns = 1,
    itemWidth = 182.5F,
    itemHeight = 40F,
    gap = 5F
  )

  private var module = modules.first()


  /**
   * Settings list (right side)
   */
  private var settings = module.getSettings()
    .map {
      when (it) {
        is CheckboxSetting -> UICheckboxSetting(it)
        is ColorSetting -> UIColorSetting(it)
        is InfoSetting -> UIInfoSetting(it)
        is KeyBindSetting -> UIKeyBindSetting(it)
        is ModeSetting -> UIModeSetting(it)
        is RangeSetting -> UIRangeSetting(it)
        is SliderSetting -> UISliderSetting(it)
        else -> UITextSetting(it as TextSetting)
      }
    }

  private val settingsScroll = ScrollHandler()
  private val settingsLayout = GridLayout(
    columns = 1,
    itemWidth = 627.5F,
    itemHeight = 60F,
    gap = 10F
  )

  init {
    components.addAll(settings)
    components.addAll(modules)
    components.add(backButton)
    components.add(topBar)
  }

  override fun render() {
    NVGRenderer.rect(x, y, width, height, Color(18, 18, 18).rgb, 10F)
    NVGRenderer.line(
      x + width / 4F,
      y + topBar.height / 2 + height * 1F / 8F,
      x + width / 4F,
      y + topBar.height / 2 + height * 7F / 8F,
      1F, Color(42, 42, 42).rgb
    )

    topBar
      .updateBounds(x, y)
      .render()

    backButton
      .updateBounds(x + 20F, y + topBar.height + 20F)
      .render()

    NVGRenderer.text(
      metadata.name,
      x + backButton.width + 35F,
      y + topBar.height + 27.5F,
      15F, Color(230, 230, 230).rgb
    )

    // Render modules list (left side)
    val startY = y + topBar.height + backButton.height + 40F
    val visibleHeight = height - (topBar.height + backButton.height + 40F)

    modulesScroll.setMaxScroll(modulesLayout.contentHeight(modules.size) + 20F, visibleHeight)
    NVGRenderer.pushScissor(x, startY, width / 4F, visibleHeight)

    val modulesScrollOffset = modulesScroll.getOffset()
    modulesLayout.layout(x + 20F, startY - modulesScrollOffset, modules)
    modules.forEach(UIComponent::render)

    NVGRenderer.popScissor()

    // Render settings list (right side)
    val settingsStartX = x + width / 4F + 20F

    settingsScroll.setMaxScroll(settingsLayout.contentHeight(settings.size) - 15F, visibleHeight + 35F)
    NVGRenderer.pushScissor(settingsStartX, startY - 35F, width * 3F / 4F - 40F, visibleHeight + 35F)

    val settingsScrollOffset = settingsScroll.getOffset()
    settingsLayout.layout(settingsStartX, startY - 35F - settingsScrollOffset, settings)
    settings.forEach(UIComponent::render)

    NVGRenderer.popScissor()
  }

  override fun mouseScrolled(horizontalAmount: Double, verticalAmount: Double): Boolean {
    if (isHoveringOver(x, y, width / 4F, height)) {
      modulesScroll.handleScroll(verticalAmount)
      return true
    }

    if (isHoveringOver(x + width / 4F, y, width * 3F / 4F, height)) {
      settingsScroll.handleScroll(verticalAmount)
      return true
    }

    return false
  }

  fun setModule(module: UIModule) {
    modules.forEach {
      when {
        it == module -> module.setSelected()
        else -> it.setSelected(false)
      }
    }

    this.module = module
    components.removeAll(settings)

    this.settings = module.getSettings()
      .map {
        when (it) {
          is CheckboxSetting -> UICheckboxSetting(it)
          is ColorSetting -> UIColorSetting(it)
          is KeyBindSetting -> UIKeyBindSetting(it)
          is ModeSetting -> UIModeSetting(it)
          is RangeSetting -> UIRangeSetting(it)
          is SliderSetting -> UISliderSetting(it)
          else -> UITextSetting(it as TextSetting)
        }
      }.also {
        components.addAll(it)
      }
  }

}
