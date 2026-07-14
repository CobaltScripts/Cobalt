package org.cobalt.ui.helper

import org.cobalt.module.type.RenderableModule
import org.cobalt.util.client.WindowUtils.scaleX
import org.cobalt.util.client.WindowUtils.scaleY
import org.cobalt.util.client.WindowUtils.windowHeight
import org.cobalt.util.client.WindowUtils.windowWidth
import org.cobalt.util.input.Mouse
import org.cobalt.util.input.Mouse.mouseX
import org.cobalt.util.input.Mouse.mouseY

class DragHandler {

  var isMoving = false
    private set

  var isResizing = false
    private set

  val isActive
    get() = isMoving || isResizing

  private var dragOffsetX = 0f
  private var dragOffsetY = 0f
  private var initialMouseX = 0f
  private var initialWidth = 0f

  fun startMove(renderX: Float, renderY: Float) {
    isMoving = true
    dragOffsetX = mouseX - renderX
    dragOffsetY = mouseY - renderY
  }

  fun tryStartResize(
    squareSize: Float,
    renderX: Float,
    renderY: Float,
    scaledWidth: Float,
    scaledHeight: Float,
  ): Boolean {
    val resScale = scaleY
    val squareSizeScaled = squareSize * resScale
    val squareOffset = squareSizeScaled / 2f

    if (
      !Mouse.isHoveringOver(
        renderX + scaledWidth - squareOffset,
        renderY + scaledHeight - squareOffset,
        squareSizeScaled, squareSizeScaled
      )
    ) {
      return false
    }

    initialMouseX = mouseX
    initialWidth = scaledWidth
    isResizing = true

    return true
  }

  fun handleDrag(
    module: RenderableModule,
    allModules: List<RenderableModule>,
    snapHelper: SnapHelper,
  ): Boolean = when {
    isResizing -> handleResize(module)
    isMoving -> handleMove(module, allModules, snapHelper)
    else -> false
  }

  private fun handleResize(module: RenderableModule): Boolean {
    val baseWidth = module.width.takeIf { it > 0f } ?: return false
    val resScale = scaleY
    val currentScaledWidth = initialWidth + (mouseX - initialMouseX)

    module.scale = (currentScaledWidth / (baseWidth * resScale)).coerceIn(0.75f, 2.0f)

    return true
  }

  private fun handleMove(
    module: RenderableModule,
    allModules: List<RenderableModule>,
    snapHelper: SnapHelper,
  ): Boolean {
    val resScale = scaleY
    val width = module.width * module.scale * resScale
    val height = module.height * module.scale * resScale

    val clampedX = (mouseX - dragOffsetX).coerceIn(0f, windowWidth - width)
    val clampedY = (mouseY - dragOffsetY).coerceIn(0f, windowHeight - height)

    val otherBounds = allModules
      .filter { it != module }
      .map { other ->
        SnapHelper.ModuleBounds(
          other.xPos * scaleX,
          other.yPos * scaleY,
          other.width * other.scale * resScale,
          other.height * other.scale * resScale
        )
      }

    val (snappedX, snappedY) = snapHelper.findAlignmentGuides(
      clampedX,
      clampedY,
      width,
      height,
      otherBounds,
    )

    module.xPos = snappedX / scaleX
    module.yPos = snappedY / scaleY

    return true
  }

  fun reset() {
    isMoving = false
    isResizing = false
    dragOffsetX = 0f
    dragOffsetY = 0f
  }

}
