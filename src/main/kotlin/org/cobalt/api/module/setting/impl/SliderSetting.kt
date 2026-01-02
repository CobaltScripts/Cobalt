package org.cobalt.api.module.setting.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import org.cobalt.api.module.setting.Setting

class SliderSetting(
  name: String,
  description: String,
  defaultValue: Double,
  val min: Double,
  val max: Double,
) : Setting<Double>(name, description, defaultValue) {

  override fun read(element: JsonElement) {
    this.value = element.asDouble.coerceIn(min, max)
  }

  override fun write(): JsonElement {
    return JsonPrimitive(value)
  }

}
