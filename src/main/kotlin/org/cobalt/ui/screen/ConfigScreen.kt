package org.cobalt.ui.screen

import net.minecraft.client.gui.GuiGraphicsExtractor
import org.cobalt.module.Module
import org.cobalt.module.ModuleCategory
import org.cobalt.module.ModuleManager
import org.cobalt.ui.UIScreen
import org.cobalt.ui.animation.BounceAnimation
import org.cobalt.ui.animation.EaseOutAnimation
import org.cobalt.ui.component.SidebarComponent
import org.cobalt.ui.component.TopbarComponent
import org.cobalt.ui.page.Page
import org.cobalt.ui.page.impl.ModulesPage
import org.cobalt.ui.theme.Theme
import org.cobalt.ui.theme.ThemeManager
import org.cobalt.util.client.WindowUtils.windowHeight
import org.cobalt.util.client.WindowUtils.windowWidth
import org.cobalt.util.render.SkiaRenderer
import org.cobalt.util.scheduling.Multithreading

object ConfigScreen : UIScreen() {

  private val openAnim = BounceAnimation(400L)
  private val closeAnim = EaseOutAnimation(100L)
  private var closing = false

  private val theme: Theme
    get() = ThemeManager.activeTheme

  var currentPage: Page = ModulesPage
    set(value) {
      if (field == value) {
        return
      }

      value.initializePage()

      components.remove(field)
      components.add(value)

      field = value
    }

  var selectedCategory: ModuleCategory = ModuleCategory.COMBAT
    set(value) {
      if (field == value) {
        return
      }

      field = value
      currentPage.initializePage()
    }

  init {
    components.add(SidebarComponent)
    components.add(TopbarComponent)
    components.add(currentPage)
  }

  override fun added() {
    closing = false
    openAnim.start()
    currentPage.initializePage()
  }

  override fun renderSkia() {
    val centerX = windowWidth / 2f
    val centerY = windowHeight / 2f

    SkiaRenderer.push()

    val scale = when {
      closing -> closeAnim.get(1f, 0f)
      openAnim.isAnimating() -> openAnim.get(0f, 1f)
      else -> 1f
    }

    if (scale != 1f) {
      SkiaRenderer.translate(centerX, centerY)
      SkiaRenderer.scale(scale, scale)
      SkiaRenderer.translate(-centerX, -centerY)
    }

    val interfaceWidth = SidebarComponent.width + ModulesPage.width
    val interfaceHeight = SidebarComponent.height

    val pageX = centerX - (interfaceWidth / 2f)
    val pageY = centerY - (interfaceHeight / 2f)

    val startX = pageX + SidebarComponent.width
    val startY = pageY + TopbarComponent.height

    currentPage
      .updateBounds(startX, startY)
      .renderComponent()

    TopbarComponent
      .updateBounds(startX, pageY)
      .renderComponent()

    SidebarComponent
      .updateBounds(pageX, pageY)
      .renderComponent()

    SkiaRenderer.roundedOutline(
      x = pageX,
      y = pageY,
      width = interfaceWidth,
      height = interfaceHeight,
      thickness = 1f,
      radius = 10f,
      color = theme.border
    )

    SkiaRenderer.line(
      x1 = startX,
      y1 = pageY,
      x2 = startX,
      y2 = pageY + ModulesPage.height,
      thickness = 1f,
      color = theme.border
    )

    SkiaRenderer.line(
      x1 = startX,
      y1 = startY,
      x2 = startX + TopbarComponent.width,
      y2 = startY,
      thickness = 1f,
      color = theme.border
    )

    SkiaRenderer.pop()

    if (closing && !closeAnim.isAnimating()) {
      closing = false
      super.onClose()
    }
  }

  fun closeScreen() {
    if (closing) {
      return
    }

    Multithreading.runAsync {
      ModuleManager.modules.forEach(Module::saveConfig)
    }

    closing = true
    closeAnim.start()
  }

  override fun onClose() {
    closeScreen()
  }

  override fun extractBlurredBackground(graphics: GuiGraphicsExtractor) = Unit
  override fun extractMenuBackground(graphics: GuiGraphicsExtractor) = Unit

}
