package org.cobalt.ui.screen

import net.minecraft.client.input.MouseButtonEvent
import org.cobalt.module.Module
import org.cobalt.module.ModuleManager
import org.cobalt.module.type.RenderableModule
import org.cobalt.ui.UIScreen
import org.cobalt.ui.helper.DragHandler
import org.cobalt.ui.helper.SnapHelper
import org.cobalt.ui.theme.Theme
import org.cobalt.ui.theme.ThemeManager
import org.cobalt.util.client.WindowUtils.scaleX
import org.cobalt.util.client.WindowUtils.scaleY
import org.cobalt.util.client.WindowUtils.windowHeight
import org.cobalt.util.client.WindowUtils.windowWidth
import org.cobalt.util.input.Mouse
import org.cobalt.util.render.SkiaRenderer
import org.cobalt.util.scheduling.Multithreading

object HudEditorScreen : UIScreen() {

  private val modules: List<RenderableModule>
    get() = ModuleManager.modules
      .filterIsInstance<RenderableModule>()
      .filter { it.enabled }

  private var selectedModule: RenderableModule? = null
  private val snapHelper = SnapHelper()
  private val dragHandler = DragHandler()

  private inline val theme: Theme
    get() = ThemeManager.activeTheme

  override fun renderSkia() {
    modules.forEach { module ->
      SkiaRenderer.push()

      val renderX = module.xPos * scaleX
      val renderY = module.yPos * scaleY
      val finalScale = module.scale * scaleY

      SkiaRenderer.translate(renderX, renderY)
      SkiaRenderer.scale(finalScale, finalScale)
      SkiaRenderer.translate(-module.xPos, -module.yPos)

      drawRenderableModule(module)

      SkiaRenderer.pop()
    }

    drawSnapGuides()
  }

  private fun drawSnapGuides() {
    if (!dragHandler.isMoving) {
      return
    }

    snapHelper.activeGuides.forEach { guide ->
      if (guide.isVertical) {
        SkiaRenderer.line(guide.position, 0f, guide.position, windowHeight, 1f, theme.accentPrimary)
      } else {
        SkiaRenderer.line(0f, guide.position, windowWidth, guide.position, 1f, theme.accentPrimary)
      }
    }
  }

  private fun drawRenderableModule(module: RenderableModule) {
    val x = module.xPos
    val y = module.yPos
    val width = module.width
    val height = module.height

    module.renderComponent()

    val isSelected = module == selectedModule

    SkiaRenderer.outline(x, y, width, height, 1f, if (isSelected) theme.accentPrimary else theme.border)

    if (isSelected) {
      val squareOffset = SQUARE_SIZE / 2.0f
      SkiaRenderer.rect(
        x + width - squareOffset, y + height - squareOffset,
        SQUARE_SIZE, SQUARE_SIZE,
        theme.accentPrimary
      )
    }
  }

  override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
    if (event.button() != 0) {
      return super.mouseClicked(event, doubleClick)
    }

    val clickedModule = modules.firstOrNull { module ->
      val renderX = module.xPos * scaleX
      val renderY = module.yPos * scaleY
      val resScale = scaleY
      val scaledWidth = module.width * module.scale * resScale
      val scaledHeight = module.height * module.scale * resScale

      if (module == selectedModule && dragHandler.tryStartResize(
          SQUARE_SIZE,
          renderX,
          renderY,
          scaledWidth,
          scaledHeight
        )
      ) {
        return@firstOrNull true
      }

      if (Mouse.isHoveringOver(renderX, renderY, scaledWidth, scaledHeight)) {
        dragHandler.startMove(renderX, renderY)
        return@firstOrNull true
      }

      false
    }

    selectedModule = clickedModule
    return clickedModule != null || super.mouseClicked(event, doubleClick)
  }

  override fun mouseDragged(event: MouseButtonEvent, dx: Double, dy: Double): Boolean {
    val module = selectedModule ?: return super.mouseDragged(event, dx, dy)

    return if (dragHandler.handleDrag(module, modules, snapHelper)) {
      true
    } else {
      super.mouseDragged(event, dx, dy)
    }
  }

  override fun mouseReleased(event: MouseButtonEvent): Boolean {
    dragHandler.reset()
    snapHelper.clearGuides()

    return super.mouseReleased(event)
  }

  override fun mouseScrolled(
    x: Double,
    y: Double,
    scrollX: Double,
    scrollY: Double,
  ): Boolean {
    val module = modules.firstOrNull { candidate ->
      val renderX = candidate.xPos * scaleX
      val renderY = candidate.yPos * scaleY
      val resScale = scaleY
      val scaledWidth = candidate.width * candidate.scale * resScale
      val scaledHeight = candidate.height * candidate.scale * resScale

      Mouse.isHoveringOver(renderX, renderY, scaledWidth, scaledHeight)
    } ?: return super.mouseScrolled(x, y, scrollX, scrollY)

    selectedModule = module

    module.scale = (module.scale + (scrollY.toFloat() * SCALE_STEP))
      .coerceIn(MIN_SCALE, MAX_SCALE)

    return true
  }

  override fun onClose() {
    super.onClose()

    Multithreading.runAsync {
      ModuleManager.modules.forEach(Module::saveConfig)
    }
  }

  private const val SQUARE_SIZE = 10.0f
  private const val SCALE_STEP = 0.05f
  private const val MIN_SCALE = 0.75f
  private const val MAX_SCALE = 2.0f

}
