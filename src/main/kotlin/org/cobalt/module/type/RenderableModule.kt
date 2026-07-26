package org.cobalt.module.type

import org.cobalt.module.Module
import org.cobalt.module.ModuleCategory
import org.cobalt.ui.theme.Theme
import org.cobalt.ui.theme.ThemeManager

abstract class RenderableModule @JvmOverloads constructor(
  name: String,
  category: ModuleCategory,
  var xPos: Float = 5f,
  var yPos: Float = 5f,
  var scale: Float = 1.0f,
) : Module(name, category) {

  inline val theme: Theme
    get() = ThemeManager.activeTheme

  abstract val width: Float
  abstract val height: Float

  abstract fun renderComponent()

}
