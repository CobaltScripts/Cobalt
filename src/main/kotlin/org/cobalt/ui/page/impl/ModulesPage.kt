package org.cobalt.ui.page.impl

import org.cobalt.module.Module
import org.cobalt.module.ModuleCategory
import org.cobalt.module.ModuleManager
import org.cobalt.ui.PADDING
import org.cobalt.ui.animation.EaseOutAnimation
import org.cobalt.ui.component.ModuleComponent
import org.cobalt.ui.helper.ScrollHelper
import org.cobalt.ui.helper.layoutMasonryColumns
import org.cobalt.ui.page.Page
import org.cobalt.ui.screen.ConfigScreen
import org.cobalt.util.failsafe.FailsafeManager
import org.cobalt.util.input.Mouse
import org.cobalt.util.render.SkiaRenderer

object ModulesPage : Page() {

  override val title: String
    get() = "Modules"

  private val moduleComponents = mutableListOf<ModuleComponent>()
  private val scrollHelper = ScrollHelper()
  private val openingOffset = EaseOutAnimation(200L)

  override fun initializePage() {
    openingOffset.start()
    resetComponents()
  }

  override fun onSearchQueryChanged(query: String) {
    resetComponents(query)
  }

  private fun addModuleComponentAndChild(module: Module) {
    val component = ModuleComponent(module)
    moduleComponents.add(component)
    addChild(component)
  }

  private fun resetComponents(query: String = "") {
    moduleComponents.clear()
    scrollHelper.reset()
    removeAllChildren()

    val modules = ModuleManager.modules
      .filter { module ->
        module.category == ConfigScreen.selectedCategory
      }

    val failsafes = FailsafeManager.failsafes
      .filter { ConfigScreen.selectedCategory == ModuleCategory.FAILSAFE }

    (modules + failsafes).filter { module ->
      query.isBlank()
        || module.name.contains(query, ignoreCase = true)
        || module.getSettings().any { setting ->
        setting.name.contains(query, ignoreCase = true)
          || setting.description.contains(query, ignoreCase = true)
      }
    }.forEach { module ->
      addModuleComponentAndChild(module)
    }
  }

  override fun renderComponent() {
    super.renderComponent()

    val pageOffset = openingOffset.get(-30f, 0f)
    val startY = yPos + PADDING + pageOffset - scrollHelper.scrollOffset

    SkiaRenderer.pushScissor(xPos, yPos, width, height)

    val maxColumnY = layoutMasonryColumns(
      items = moduleComponents,
      columns = 2,
      startY = startY,
      gap = PADDING,
      columnX = { col -> xPos + PADDING + col * (ModuleComponent.WIDTH + COLUMN_GAP) },
      heightOf = { it.height },
      place = { component, x, y -> component.updateBounds(x, y).renderComponent() }
    )

    SkiaRenderer.popScissor()

    val contentHeight = maxColumnY + scrollHelper.scrollOffset - yPos
    scrollHelper.updateMaxScroll(contentHeight, height)
  }


  override fun mouseScrolled(horizontalAmount: Double, verticalAmount: Double): Boolean {
    if (Mouse.isHoveringOver(xPos, yPos, width, height)) {
      scrollHelper.scroll(verticalAmount)
      return true
    }

    return false
  }

  private const val COLUMN_GAP = 20f

}
