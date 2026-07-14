package org.cobalt.ui.component.setting

import com.google.gson.JsonElement
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import org.cobalt.ui.UIComponent
import org.cobalt.ui.component.ModuleComponent
import org.cobalt.ui.component.setting.impl.InfoSetting
import org.cobalt.util.config.SettingContainer
import org.cobalt.util.render.SkiaRenderer

abstract class Setting<T>(
  val name: String,
  val description: String,
  var value: T,
) : ReadWriteProperty<SettingContainer, T>,
  PropertyDelegateProvider<SettingContainer, ReadWriteProperty<SettingContainer, T>>,
  UIComponent(
    width = ModuleComponent.WIDTH,
    height = BASE_HEIGHT
  ) {

  val defaultValue: T = value

  abstract fun renderSetting()

  override fun renderComponent() {
    if (this !is InfoSetting) {
      val extraHeight = if (description.isNotBlank()) DESCRIPTION_SIZE + TEXT_SPACING_Y else 0f
      val totalTextHeight = NAME_SIZE + extraHeight
      val textStartY = yPos + (BASE_HEIGHT - totalTextHeight) / 2

      SkiaRenderer.text(
        SkiaRenderer.regularFont, name,
        xPos + PADDING, textStartY,
        NAME_SIZE, theme.textPrimary
      )

      if (description.isNotBlank()) {
        SkiaRenderer.text(
          SkiaRenderer.regularFont, description,
          xPos + PADDING, textStartY + NAME_SIZE + TEXT_SPACING_Y,
          DESCRIPTION_SIZE, theme.textMuted
        )
      }
    }

    renderSetting()
  }

  override operator fun provideDelegate(
    thisRef: SettingContainer,
    property: KProperty<*>,
  ): ReadWriteProperty<SettingContainer, T> {
    thisRef.addSettings(this)
    return this
  }

  override operator fun getValue(thisRef: SettingContainer, property: KProperty<*>): T {
    return value
  }

  override operator fun setValue(thisRef: SettingContainer, property: KProperty<*>, value: T) {
    this.value = value
  }

  abstract fun read(element: JsonElement)
  abstract fun write(): JsonElement

  companion object {
    protected const val BASE_HEIGHT = 60f
    protected const val PADDING = 20f
    protected const val NAME_SIZE = 14f
    protected const val DESCRIPTION_SIZE = 12f
    protected const val TEXT_SPACING_Y = 5f
  }

}
