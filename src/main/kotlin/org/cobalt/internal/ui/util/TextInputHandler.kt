package org.cobalt.internal.ui.util

class TextInputHandler(
  private val maxLength: Int = 100,
  private val allowedChars: (Char) -> Boolean = { true }
) {
  private var text = StringBuilder()

  fun getText(): String = text.toString()

  fun setText(newText: String) {
    text = StringBuilder(newText.take(maxLength))
  }

  fun handleChar(char: Char): Boolean {
    if (char == '\b') {
      if (text.isNotEmpty()) {
        text.deleteCharAt(text.length - 1)
      }
      return true
    }

    if (text.length < maxLength && allowedChars(char) && char.code >= 32) {
      text.append(char)
      return true
    }

    return false
  }

  fun clear() {
    text.clear()
  }
}

