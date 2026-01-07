package org.cobalt.internal.ui.components.settings

import java.awt.Color
import org.cobalt.Cobalt.mc
import org.cobalt.api.module.setting.impl.ModeSetting
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.ui.UIComponent
import org.cobalt.internal.ui.components.tooltips.TooltipPosition
import org.cobalt.internal.ui.components.tooltips.UITooltip
import org.cobalt.internal.ui.components.tooltips.impl.UITextTooltip
import org.cobalt.internal.ui.util.isHoveringOver
import org.cobalt.internal.ui.util.mouseY
import kotlin.math.min

internal class UIModeSetting(private val setting: ModeSetting) : UIComponent(
  x = 0F,
  y = 0F,
  width = 627.5F,
  height = 60F,
) {

  private var isExpanded = false
  private val optionHeight = 28F
  private val minWidth = 80F
  private val padding = 40F
  private val maxVisibleOptions = 6
  private var scrollOffset = 0

  private val tooltip = UITooltip(
    content = { UITextTooltip(setting.description) },
    position = TooltipPosition.LEFT
  )

  private fun calculateButtonWidth(): Float {
    val currentOption = setting.options.getOrElse(setting.value) { "Unknown" }
    val textWidth = NVGRenderer.textWidth(currentOption, 13F)
    return maxOf(minWidth, textWidth + padding)
  }

  private fun calculateDropdownWidth(): Float {
    val maxTextWidth = setting.options.maxOfOrNull { NVGRenderer.textWidth(it, 13F) } ?: 0F
    return maxOf(minWidth, maxTextWidth + padding)
  }

  private fun getVisibleOptionsCount(buttonY: Float, buttonHeight: Float): Int {
    val screenHeight = mc.window.height.toFloat()
    val dropdownY = buttonY + buttonHeight + 4F
    val availableHeight = screenHeight - dropdownY - 10F
    val maxByScreen = (availableHeight / optionHeight).toInt()
    return min(maxVisibleOptions, min(maxByScreen, setting.options.size)).coerceAtLeast(1)
  }

  override fun render() {
    NVGRenderer.rect(x, y, width, height, Color(42, 42, 42, 50).rgb, 10F)
    NVGRenderer.hollowRect(x, y, width, height, 1F, Color(42, 42, 42).rgb, 10F)

    NVGRenderer.text(
      setting.name,
      x + 20F,
      y + (height / 2F) - 15.5F,
      15F,
      Color(230, 230, 230).rgb
    )

    NVGRenderer.text(
      setting.description,
      x + 20F,
      y + (height / 2F) + 2F,
      12F,
      Color(179, 179, 179).rgb
    )

    val buttonWidth = calculateButtonWidth()
    val buttonX = x + width - buttonWidth - 20F
    val buttonY = y + (height / 2F) - 14F
    val buttonHeight = 28F
    val currentOption = setting.options.getOrElse(setting.value) { "Unknown" }

    NVGRenderer.rect(buttonX, buttonY, buttonWidth, buttonHeight, Color(42, 42, 42, 50).rgb, 6F)
    NVGRenderer.hollowRect(buttonX, buttonY, buttonWidth, buttonHeight, 1.5F, Color(61, 94, 149).rgb, 6F)

    NVGRenderer.text(
      currentOption,
      buttonX + 12F,
      buttonY + 7F,
      13F,
      Color(200, 200, 200).rgb
    )

    NVGRenderer.image(
      if (isExpanded) arrowUpIcon else arrowDownIcon,
      buttonX + buttonWidth - 22F,
      buttonY + 6F,
      16F,
      16F,
      colorMask = Color(255, 255, 255).rgb
    )

    if (isExpanded) {
      val dropdownWidth = calculateDropdownWidth()
      val dropdownX = x + width - dropdownWidth - 20F
      val visibleCount = getVisibleOptionsCount(buttonY, buttonHeight)
      renderDropdown(dropdownX, buttonY + buttonHeight + 4F, dropdownWidth, visibleCount)
    }

    tooltip.updateBounds(buttonX, buttonY, buttonWidth, buttonHeight)
  }

  private fun renderDropdown(dropdownX: Float, dropdownY: Float, dropdownWidth: Float, visibleCount: Int) {
    val needsScrolling = setting.options.size > visibleCount
    val maxScrollOffset = if (needsScrolling) setting.options.size - visibleCount else 0
    scrollOffset = scrollOffset.coerceIn(0, maxScrollOffset)

    val dropdownHeight = optionHeight * visibleCount

    NVGRenderer.rect(dropdownX, dropdownY, dropdownWidth, dropdownHeight, Color(25, 25, 25).rgb, 6F)
    NVGRenderer.hollowRect(dropdownX, dropdownY, dropdownWidth, dropdownHeight, 1.5F, Color(42, 42, 42).rgb, 6F)

    NVGRenderer.pushScissor(dropdownX, dropdownY, dropdownWidth, dropdownHeight)

    for (i in 0 until visibleCount) {
      val index = i + scrollOffset
      if (index >= setting.options.size) break

      val option = setting.options[index]
      val optionY = dropdownY + (i * optionHeight)
      val isSelected = index == setting.value
      val isHovered = isHoveringOver(dropdownX, optionY, dropdownWidth, optionHeight)

      if (isHovered || isSelected) {
        val bgColor = if (isSelected) Color(61, 94, 149, 80) else Color(42, 42, 42, 80)
        NVGRenderer.rect(
          dropdownX + 4F,
          optionY + 2F,
          dropdownWidth - 8F,
          optionHeight - 4F,
          bgColor.rgb,
          4F
        )
      }

      NVGRenderer.text(
        option,
        dropdownX + 12F,
        optionY + 7F,
        13F,
        if (isSelected) Color(61, 94, 149).rgb else Color(200, 200, 200).rgb
      )
    }

    NVGRenderer.popScissor()

    if (needsScrolling) {
      val scrollbarWidth = 4F
      val scrollbarX = dropdownX + dropdownWidth - scrollbarWidth - 4F
      val scrollbarHeight = dropdownHeight * (visibleCount.toFloat() / setting.options.size)
      val scrollbarMaxY = dropdownHeight - scrollbarHeight
      val scrollbarY = dropdownY + (scrollbarMaxY * (scrollOffset.toFloat() / maxScrollOffset))

      NVGRenderer.rect(scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight, Color(61, 94, 149, 150).rgb, 2F)
    }
  }

  override fun mouseClicked(button: Int): Boolean {
    if (button != 0) return false

    val buttonWidth = calculateButtonWidth()
    val buttonX = x + width - buttonWidth - 20F
    val buttonY = y + (height / 2F) - 14F
    val buttonHeight = 28F

    if (isHoveringOver(buttonX, buttonY, buttonWidth, buttonHeight)) {
      isExpanded = !isExpanded
      if (isExpanded) {
        scrollOffset = 0
      }
      return true
    }

    if (isExpanded) {
      val dropdownWidth = calculateDropdownWidth()
      val dropdownX = x + width - dropdownWidth - 20F
      val dropdownY = buttonY + buttonHeight + 4F
      val visibleCount = getVisibleOptionsCount(buttonY, buttonHeight)

      for (i in 0 until visibleCount) {
        val index = i + scrollOffset
        if (index >= setting.options.size) break

        val optionY = dropdownY + (i * optionHeight)

        if (isHoveringOver(dropdownX, optionY, dropdownWidth, optionHeight)) {
          setting.value = index
          isExpanded = false
          return true
        }
      }

      isExpanded = false
      return true
    }

    return false
  }

  override fun mouseScrolled(horizontalAmount: Double, verticalAmount: Double): Boolean {
    if (!isExpanded) return false

    val buttonWidth = calculateButtonWidth()
    val buttonY = y + (height / 2F) - 14F
    val buttonHeight = 28F
    val dropdownWidth = calculateDropdownWidth()
    val dropdownX = x + width - dropdownWidth - 20F
    val dropdownY = buttonY + buttonHeight + 4F
    val visibleCount = getVisibleOptionsCount(buttonY, buttonHeight)
    val dropdownHeight = optionHeight * visibleCount

    if (isHoveringOver(dropdownX, dropdownY, dropdownWidth, dropdownHeight)) {
      val maxScrollOffset = setting.options.size - visibleCount
      if (maxScrollOffset > 0) {
        scrollOffset = (scrollOffset - verticalAmount.toInt()).coerceIn(0, maxScrollOffset)
        return true
      }
    }

    return false
  }

  companion object {
    private val arrowUpIcon = NVGRenderer.createImage("/assets/cobalt/icons/arrow-up.svg")
    private val arrowDownIcon = NVGRenderer.createImage("/assets/cobalt/icons/arrow-down.svg")
  }

}
