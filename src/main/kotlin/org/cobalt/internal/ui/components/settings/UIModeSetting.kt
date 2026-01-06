package org.cobalt.internal.ui.components.settings

import java.awt.Color
import org.cobalt.api.module.setting.impl.ModeSetting
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.ui.UIComponent
import org.cobalt.internal.ui.util.isHoveringOver

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

  private fun calculateButtonWidth(): Float {
    val currentOption = setting.options.getOrElse(setting.value) { "Unknown" }
    val textWidth = NVGRenderer.textWidth(currentOption, 13F)
    return maxOf(minWidth, textWidth + padding)
  }

  private fun calculateDropdownWidth(): Float {
    val maxTextWidth = setting.options.maxOfOrNull { NVGRenderer.textWidth(it, 13F) } ?: 0F
    return maxOf(minWidth, maxTextWidth + padding)
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

    NVGRenderer.text(
      if (isExpanded) "▲" else "▼",
      buttonX + buttonWidth - 20F,
      buttonY + 7F,
      12F,
      Color(61, 94, 149).rgb
    )

    if (isExpanded) {
      val dropdownWidth = calculateDropdownWidth()
      val dropdownX = x + width - dropdownWidth - 20F
      renderDropdown(dropdownX, buttonY + buttonHeight + 4F, dropdownWidth)
    }
  }

  private fun renderDropdown(dropdownX: Float, dropdownY: Float, dropdownWidth: Float) {
    val dropdownHeight = optionHeight * setting.options.size

    NVGRenderer.rect(dropdownX, dropdownY, dropdownWidth, dropdownHeight, Color(25, 25, 25).rgb, 6F)
    NVGRenderer.hollowRect(dropdownX, dropdownY, dropdownWidth, dropdownHeight, 1.5F, Color(42, 42, 42).rgb, 6F)

    for ((index, option) in setting.options.withIndex()) {
      val optionY = dropdownY + (index * optionHeight)
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
  }

  override fun mouseClicked(button: Int): Boolean {
    if (button != 0) return false

    val buttonWidth = calculateButtonWidth()
    val buttonX = x + width - buttonWidth - 20F
    val buttonY = y + (height / 2F) - 14F
    val buttonHeight = 28F

    if (isHoveringOver(buttonX, buttonY, buttonWidth, buttonHeight)) {
      isExpanded = !isExpanded
      return true
    }

    if (isExpanded) {
      val dropdownWidth = calculateDropdownWidth()
      val dropdownX = x + width - dropdownWidth - 20F
      val dropdownY = buttonY + buttonHeight + 4F

      for ((index, _) in setting.options.withIndex()) {
        val optionY = dropdownY + (index * optionHeight)

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

}
