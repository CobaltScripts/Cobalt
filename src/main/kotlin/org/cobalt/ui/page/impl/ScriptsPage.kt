package org.cobalt.ui.page.impl

import org.cobalt.module.ModuleManager
import org.cobalt.module.type.Script
import org.cobalt.ui.animation.EaseOutAnimation
import org.cobalt.ui.component.ScriptComponent
import org.cobalt.ui.helper.ScrollHelper
import org.cobalt.ui.page.Page
import org.cobalt.util.input.Mouse
import org.cobalt.util.render.SkiaRenderer

object ScriptsPage : Page() {

  override val title: String
    get() = "Scripts"

  private val scriptComponents = mutableListOf<Pair<ScriptComponent, String>>()
  private val scrollHelper = ScrollHelper()
  private val openingOffset = EaseOutAnimation(200L)

  override fun initializePage() {
    openingOffset.start()
    resetComponents()
  }

  override fun onSearchQueryChanged(query: String) {
    resetComponents(query)
  }

  private fun resetComponents(query: String = "") {
    scriptComponents.clear()
    scrollHelper.reset()

    removeAllChildren()

    ModuleManager.modules
      .filterIsInstance<Script>()
      .filter { script ->
        query.isBlank() ||
          script.name.contains(query, ignoreCase = true) ||
          script.getSettings().any { setting ->
            setting.name.contains(query, ignoreCase = true) ||
              setting.description.contains(query, ignoreCase = true)
          }
      }
      .groupBy { it.category }
      .forEach { (category, scripts) ->
        scripts.forEach { script ->
          val component = ScriptComponent(script)
          scriptComponents.add(Pair(component, category.name))
          addChild(component)
        }
      }
  }

  override fun renderComponent() {
    super.renderComponent()

    val pageOffset = openingOffset.get(-30f, 0f)
    var currentY = yPos + PADDING + pageOffset - scrollHelper.scrollOffset

    SkiaRenderer.pushScissor(xPos, yPos, width, height)

    scriptComponents
      .groupBy { (_, category) -> category }
      .forEach { (category, components) ->
        SkiaRenderer.text(
          font = SkiaRenderer.regularFont,
          text = category,
          x = xPos + PADDING,
          y = currentY,
          size = CATEGORY_FONT_SIZE,
          color = theme.textMuted
        )

        currentY += CATEGORY_FONT_SIZE + CATEGORY_MARGIN

        val columnY = floatArrayOf(currentY, currentY)
        val columnX = floatArrayOf(
          xPos + PADDING,
          xPos + PADDING + ScriptComponent.WIDTH + COLUMN_GAP
        )

        components.forEachIndexed { index, (component, _) ->
          val col = index % 2

          component
            .updateBounds(columnX[col], columnY[col])
            .renderComponent()

          columnY[col] += component.height + PADDING
        }

        currentY = columnY.max() + GROUP_GAP
      }

    SkiaRenderer.popScissor()

    val contentHeight = currentY + scrollHelper.scrollOffset - yPos
    scrollHelper.updateMaxScroll(contentHeight, height)
  }

  override fun mouseScrolled(horizontalAmount: Double, verticalAmount: Double): Boolean {
    if (Mouse.isHoveringOver(xPos, yPos, width, height)) {
      scrollHelper.scroll(verticalAmount)
      return true
    }

    return false
  }

  private const val PADDING = 20f
  private const val COLUMN_GAP = 20f
  private const val GROUP_GAP = 28f
  private const val CATEGORY_FONT_SIZE = 12f
  private const val CATEGORY_MARGIN = 20f

}
