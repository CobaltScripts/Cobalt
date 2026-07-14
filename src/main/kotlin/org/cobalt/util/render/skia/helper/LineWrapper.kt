package org.cobalt.util.render.skia.helper

class LineWrapper(
  private val maxWidth: Float,
  private val measureWidth: (String) -> Float,
) {

  private val lines = mutableListOf<String>()
  private val line = StringBuilder()

  fun wrap(text: String): List<String> {
    text.split('\n').forEach { paragraph ->
      paragraph.split(' ').filter { it.isNotEmpty() }.forEach { word ->
        processWord(word)
      }

      if (line.isNotEmpty()) {
        pushLine()
      }
    }

    return lines
  }

  private fun processWord(word: String) {
    val candidate = if (line.isEmpty()) word else "$line $word"

    if (line.isNotEmpty() && measureWidth(candidate) > maxWidth) {
      pushLine()
    }

    if (measureWidth(word) <= maxWidth) {
      append(word)
    } else {
      splitAndAppendLongWord(word)
    }
  }

  private fun splitAndAppendLongWord(word: String) {
    val chunk = StringBuilder()

    for (char in word) {
      if (chunk.isNotEmpty() && measureWidth("$chunk$char") > maxWidth) {
        append(chunk.toString())
        pushLine()
        chunk.clear()
      }

      chunk.append(char)
    }

    append(chunk.toString())
  }

  private fun pushLine() {
    lines.add(line.toString())
    line.clear()
  }

  private fun append(word: String) {
    if (line.isNotEmpty()) {
      line.append(' ')
    }

    line.append(word)
  }

}
