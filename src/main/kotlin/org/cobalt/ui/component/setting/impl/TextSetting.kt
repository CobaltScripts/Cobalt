package org.cobalt.ui.component.setting.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import org.cobalt.ui.component.TextInputComponent
import org.cobalt.ui.component.setting.Setting

class TextSetting(
  name: String,
  description: String,
  defaultValue: String,
  val placeholder: String = "Enter text...",
) : Setting<String>(name, description, defaultValue) {

  private val inputBox = TextInputComponent(
    width = INPUT_BOX_WIDTH,
    height = INPUT_BOX_HEIGHT,
    fontSize = 12f,
    placeholder = placeholder,
    onChange = { query ->
      value = query
    }
  )

  init {
    addChild(inputBox)
  }

  override fun read(element: JsonElement) {
    value = element.asString
    inputBox.updateContent(value)
  }

  override fun write(): JsonElement {
    return JsonPrimitive(value)
  }

  override fun renderSetting() {
    val inputBoxX = xPos + width - INPUT_BOX_WIDTH - PADDING
    val inputBoxY = yPos + (height - INPUT_BOX_HEIGHT) / 2

    inputBox
      .updateBounds(inputBoxX, inputBoxY)
      .renderComponent()
  }

  companion object {
    private const val INPUT_BOX_WIDTH = 175f
    private const val INPUT_BOX_HEIGHT = 35f
  }

}
