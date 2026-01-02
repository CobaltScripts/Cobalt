package org.cobalt.internal.ui.components.tooltips

import java.awt.Color
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.ui.UIComponent
import org.cobalt.internal.ui.animation.Animation
import org.cobalt.internal.ui.animation.ColorAnimation
import org.cobalt.internal.ui.util.isHoveringOver

enum class TooltipPosition {
  ABOVE, BELOW, LEFT, RIGHT
}

internal class UITooltip(
  private val content: () -> UIComponent,
  private val position: TooltipPosition = TooltipPosition.ABOVE,
  private val padding: Float = 8F,
  private val animation: Animation<*>? = null
) : UIComponent(0f, 0f, 0f, 0f) {

  private var targetWidth = 0F
  private var targetHeight = 0F
  private var isHovering = false
  private val alphaAnim = if (animation is ColorAnimation) animation else ColorAnimation(150L)
  private var wasHovering = false

  init {
    TooltipManager.register(this)
  }

  fun updateBounds(targetX: Float, targetY: Float, targetWidth: Float, targetHeight: Float): UIComponent {
    this.x = targetX
    this.y = targetY
    this.targetWidth = targetWidth
    this.targetHeight = targetHeight
    return this
  }

  private fun calculatePosition(contentWidth: Float, contentHeight: Float): Pair<Float, Float> {
    return when (position) {
      TooltipPosition.ABOVE -> Pair(
        x + (targetWidth / 2F) - (contentWidth / 2F),
        y - contentHeight - padding
      )
      TooltipPosition.BELOW -> Pair(
        x + (targetWidth / 2F) - (contentWidth / 2F),
        y + targetHeight + padding
      )
      TooltipPosition.LEFT -> Pair(
        x - contentWidth - padding,
        y + (targetHeight / 2F) - (contentHeight / 2F)
      )
      TooltipPosition.RIGHT -> Pair(
        x + targetWidth + padding,
        y + (targetHeight / 2F) - (contentHeight / 2F)
      )
    }
  }

  override fun render() {
    isHovering = isHoveringOver(x, y, targetWidth, targetHeight)

    if (isHovering != wasHovering) {
      animation?.start()
      alphaAnim.start()
      wasHovering = isHovering
    }

    if (isHovering) {
      val tooltipContent = content()
      val (tooltipX, tooltipY) = calculatePosition(tooltipContent.width, tooltipContent.height)

      val bgColor = alphaAnim.get(
        Color(18, 18, 18, 0),
        Color(18, 18, 18, 240),
        !isHovering
      )

      NVGRenderer.rect(tooltipX, tooltipY, tooltipContent.width, tooltipContent.height, bgColor.rgb, 4F)
      NVGRenderer.hollowRect(tooltipX, tooltipY, tooltipContent.width, tooltipContent.height, 1.5F,  Color(42, 42, 42).rgb, 4F)

      tooltipContent.x = tooltipX
      tooltipContent.y = tooltipY
      tooltipContent.render()
    }
  }
}
