package org.cobalt.api.module.setting.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import org.cobalt.api.module.setting.Setting

class ModeSetting(
  name: String,
  description: String,
  defaultValue: Int,
  val options: Array<String>,
) : Setting<Int>(name, description, defaultValue) {

  override fun read(element: JsonElement) {
    this.value = element.asInt
  }

  override fun write(): JsonElement {
    return JsonPrimitive(value)
  }

}
