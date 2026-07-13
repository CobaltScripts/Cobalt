package org.cobalt.util.chat

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor

object ChatFormatter {

  private const val FORMAT_CODE = '\u00A7'
  private const val LEGACY_HEX_LENGTH = 14
  private const val GRADIENT_CLOSE_TAG = "</gradient>"

  private val colorTags = mapOf(
    "black" to ChatFormatting.BLACK,
    "dark_blue" to ChatFormatting.DARK_BLUE,
    "dark_green" to ChatFormatting.DARK_GREEN,
    "dark_aqua" to ChatFormatting.DARK_AQUA,
    "dark_red" to ChatFormatting.DARK_RED,
    "dark_purple" to ChatFormatting.DARK_PURPLE,
    "gold" to ChatFormatting.GOLD,
    "gray" to ChatFormatting.GRAY,
    "grey" to ChatFormatting.GRAY,
    "dark_gray" to ChatFormatting.DARK_GRAY,
    "dark_grey" to ChatFormatting.DARK_GRAY,
    "blue" to ChatFormatting.BLUE,
    "green" to ChatFormatting.GREEN,
    "aqua" to ChatFormatting.AQUA,
    "red" to ChatFormatting.RED,
    "light_purple" to ChatFormatting.LIGHT_PURPLE,
    "yellow" to ChatFormatting.YELLOW,
    "white" to ChatFormatting.WHITE
  )

  private val styleTags = setOf(
    "bold",
    "b",
    "italic",
    "i",
    "underlined",
    "underline",
    "u",
    "strikethrough",
    "st",
    "obfuscated",
    "obf"
  )

  fun parse(text: String, baseStyle: Style = Style.EMPTY): MutableComponent {
    return ChatParser(text, baseStyle).parse()
  }

  private fun applyTag(
    rawTag: String,
    currentStyle: Style,
    styleStack: ArrayDeque<Style>,
    baseStyle: Style,
  ): Style? {
    val tag = rawTag.trim().lowercase()

    return when {
      tag.startsWith("/") -> applyClosingTag(tag.drop(1), styleStack)
      tag == "reset" -> {
        styleStack.clear()
        baseStyle
      }

      else -> applyOpeningTag(tag, currentStyle)?.also { styleStack.addLast(currentStyle) }
    }
  }

  private fun applyClosingTag(tagName: String, styleStack: ArrayDeque<Style>): Style? =
    if ((tagName in colorTags || tagName in styleTags) && styleStack.isNotEmpty()) {
      styleStack.removeLast()
    } else {
      null
    }

  private fun applyOpeningTag(tag: String, currentStyle: Style): Style? {
    colorTags[tag]?.let { return currentStyle.withColor(it) }
    parseHexColorTag(tag)?.let { return currentStyle.withColor(TextColor.fromRgb(it)) }

    return when (tag) {
      "bold", "b" -> currentStyle.withBold(true)
      "italic", "i" -> currentStyle.withItalic(true)
      "underlined", "underline", "u" -> currentStyle.withUnderlined(true)
      "strikethrough", "st" -> currentStyle.withStrikethrough(true)
      "obfuscated", "obf" -> currentStyle.withObfuscated(true)
      else -> null
    }
  }

  private fun parseGradientTag(tag: String): Pair<Int, Int>? {
    val parts = tag.trim().split(':')

    if (parts.size < 3 || !parts[0].equals("gradient", ignoreCase = true)) {
      return null
    }

    val startColor = parseHexColor(parts[1]) ?: return null
    val endColor = parseHexColor(parts[2]) ?: return null

    return startColor to endColor
  }

  private fun parseHexColorTag(tag: String): Int? {
    return when {
      tag.startsWith("#") -> parseHexColor(tag)
      tag.startsWith("color:#") -> parseHexColor(tag.removePrefix("color:"))
      tag.startsWith("color:") -> parseHexColor(tag.removePrefix("color:"))
      else -> null
    }
  }

  private fun parseHexColor(value: String): Int? {
    val hex = value.removePrefix("#")

    if (hex.length != 6 || hex.any { !it.isHexDigit() }) {
      return null
    }

    return hex.toInt(16)
  }

  private fun parseLegacyHex(text: String, startIndex: Int): Int? {
    if (startIndex + 13 >= text.length || text[startIndex + 1].lowercaseChar() != 'x') {
      return null
    }

    val hex = StringBuilder(6)
    var index = startIndex + 2

    repeat(6) {
      if (text[index] != FORMAT_CODE || !text[index + 1].isHexDigit()) {
        return null
      }

      hex.append(text[index + 1])
      index += 2
    }

    return hex.toString().toInt(16)
  }

  private fun Char.isHexDigit(): Boolean {
    return this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'
  }

  private class ChatParser(private val text: String, private val baseStyle: Style) {

    private val result = Component.empty()
    private val styleStack = ArrayDeque<Style>()
    private val chunk = StringBuilder()
    private var style = baseStyle
    private var index = 0

    fun parse(): MutableComponent {
      while (index < text.length) {
        val consumed = when {
          text[index] == '<' -> tryConsumeTag()
          text[index] == FORMAT_CODE && index + 1 < text.length -> tryConsumeLegacyCode()
          else -> false
        }

        if (!consumed) {
          chunk.append(text[index])
          index++
        }
      }

      flush()
      return result
    }

    private fun flush() {
      if (chunk.isEmpty()) {
        return
      }

      result.append(Component.literal(chunk.toString()).setStyle(style))
      chunk.clear()
    }

    private fun tryConsumeTag(): Boolean {
      val tagEnd = text.indexOf('>', index + 1)

      if (tagEnd == -1) {
        return false
      }

      val tag = text.substring(index + 1, tagEnd)
      val gradient = parseGradientTag(tag)

      if (gradient != null && tryConsumeGradient(tagEnd, gradient)) {
        return true
      }

      val nextStyle = applyTag(tag, style, styleStack, baseStyle) ?: return false

      flush()
      style = nextStyle
      index = tagEnd + 1
      return true
    }

    private fun tryConsumeGradient(tagEnd: Int, gradient: Pair<Int, Int>): Boolean {
      val closeStart = text.indexOf(GRADIENT_CLOSE_TAG, tagEnd + 1, ignoreCase = true)

      if (closeStart == -1) {
        return false
      }

      flush()

      result.append(
        ChatUtils.buildGradientComponent(
          text.substring(tagEnd + 1, closeStart),
          gradient.first,
          gradient.second,
          style
        )
      )

      index = closeStart + GRADIENT_CLOSE_TAG.length
      return true
    }

    private fun tryConsumeLegacyCode(): Boolean {
      val hexColor = parseLegacyHex(text, index)

      if (hexColor != null) {
        flush()
        style = baseStyle.withColor(TextColor.fromRgb(hexColor))
        index += LEGACY_HEX_LENGTH
        return true
      }

      val formatting = ChatFormatting.getByCode(text[index + 1].lowercaseChar()) ?: return false
      flush()

      style = if (formatting == ChatFormatting.RESET) {
        styleStack.clear()
        baseStyle
      } else {
        style.applyLegacyFormat(formatting)
      }

      index += 2
      return true
    }
  }

}
