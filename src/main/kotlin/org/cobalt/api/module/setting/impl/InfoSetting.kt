package org.cobalt.api.module.setting.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import org.cobalt.api.module.setting.Setting

enum class InfoType {
  INFO, WARNING, SUCCESS, ERROR
}

internal class InfoSetting(
  name: String?,
  val text: String,
  val type: InfoType = InfoType.INFO
) : Setting<String>(name ?: "", "Info", "") {

  override fun read(element: JsonElement) {}
  override fun write(): JsonElement = JsonPrimitive("")
}

