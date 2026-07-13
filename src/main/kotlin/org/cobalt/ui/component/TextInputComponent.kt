package org.cobalt.ui.component

import java.util.function.Consumer
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import org.cobalt.ui.UIComponent
import org.cobalt.ui.helper.TextInputHelper
import org.cobalt.util.color.updateAlpha
import org.cobalt.util.input.Mouse
import org.cobalt.util.render.SkiaRenderer

class TextInputComponent(
  width: Float,
  height: Float,
  onChange: Consumer<String>,
  val placeholder: String,
  val fontSize: Float,
  val type: Type = Type.DEFAULT,
) : UIComponent(
  width = width,
  height = height
) {

  private val inputHandler = TextInputHelper(fontSize, type, onChange)
  private var xOffset: Float = 0f

  override fun renderComponent() {
    inputHandler.updateBounds(xPos, yPos, width, height)

    SkiaRenderer.roundedRect(
      xPos, yPos,
      width, height,
      5f, theme.backgroundPrimary
    )

    val textColor = if (inputHandler.focused) theme.textPrimary else theme.textMuted
    val currentText = getCurrentText()
    val maxTextWidth = width - TEXT_PADDING * 2
    val caretOffset = getCaretOffset()

    updateScrollOffset(currentText, maxTextWidth, caretOffset)

    val textX = xPos + TEXT_PADDING - xOffset
    val textY = yPos + (height - fontSize) / 2

    SkiaRenderer.pushScissor(xPos, yPos, width, height)

    drawSelection(textX, textY)

    SkiaRenderer.text(
      SkiaRenderer.regularFont,
      currentText,
      textX, textY,
      fontSize, textColor
    )

    if (inputHandler.focused && (System.currentTimeMillis() / 500) % 2 == 0L) {
      val caretX = textX + caretOffset
      SkiaRenderer.rect(
        caretX, textY,
        1.5f, fontSize,
        theme.textPrimary
      )
    }

    SkiaRenderer.popScissor()
    SkiaRenderer.roundedOutline(
      xPos, yPos,
      width, height,
      1f, 5f, theme.border
    )
  }

  fun updateContent(content: String) {
    inputHandler.text = content
  }

  private fun drawSelection(textX: Float, textY: Float) {
    if (!inputHandler.hasSelection) {
      return
    }

    val selStartPrefix = if (type == Type.PASSWORD) {
      "*".repeat(inputHandler.selectionStart)
    } else {
      inputHandler.text.substring(0, inputHandler.selectionStart)
    }

    val selEndPrefix = if (type == Type.PASSWORD) {
      "*".repeat(inputHandler.selectionEnd)
    } else {
      inputHandler.text.substring(0, inputHandler.selectionEnd)
    }

    val selStartX = textX + SkiaRenderer.textWidth(SkiaRenderer.regularFont, selStartPrefix, fontSize)
    val selEndX = textX + SkiaRenderer.textWidth(SkiaRenderer.regularFont, selEndPrefix, fontSize)
    val selWidth = selEndX - selStartX
    val selectionColor = theme.textPrimary.updateAlpha(40)

    SkiaRenderer.rect(
      selStartX, textY,
      selWidth, fontSize,
      selectionColor
    )
  }

  private fun getCurrentText(): String {
    if (inputHandler.usePlaceholder) {
      return placeholder
    }

    return if (type == Type.PASSWORD) {
      "*".repeat(inputHandler.text.length)
    } else {
      inputHandler.text
    }
  }

  private fun getCaretOffset(): Float {
    val caretPrefix = if (type == Type.PASSWORD) {
      "*".repeat(inputHandler.caretIndex)
    } else {
      inputHandler.text.substring(0, inputHandler.caretIndex)
    }

    return SkiaRenderer.textWidth(SkiaRenderer.regularFont, caretPrefix, fontSize)
  }

  private fun updateScrollOffset(currentText: String, maxTextWidth: Float, caretOffset: Float) {
    val textWidth = SkiaRenderer.textWidth(SkiaRenderer.regularFont, currentText, fontSize)

    if (textWidth <= maxTextWidth) {
      xOffset = 0f
    } else {
      if (caretOffset - xOffset > maxTextWidth) {
        xOffset = caretOffset - maxTextWidth
      } else if (caretOffset - xOffset < 0f) {
        xOffset = caretOffset
      }
    }
  }

  override fun mouseClicked(button: Int): Boolean {
    val relativeX = Mouse.mouseX - (xPos + TEXT_PADDING - xOffset)
    return inputHandler.mouseClicked(button, relativeX) || super.mouseClicked(button)
  }

  override fun mouseReleased(button: Int): Boolean {
    return inputHandler.mouseReleased(button) || super.mouseReleased(button)
  }

  override fun mouseDragged(button: Int, offsetX: Double, offsetY: Double): Boolean {
    val relativeX = Mouse.mouseX - (xPos + TEXT_PADDING - xOffset)
    return inputHandler.mouseDragged(button, relativeX) || super.mouseDragged(button, offsetX, offsetY)
  }

  override fun charTyped(input: CharacterEvent): Boolean {
    return inputHandler.charTyped(input) || super.charTyped(input)
  }

  override fun keyPressed(input: KeyEvent): Boolean {
    return inputHandler.keyPressed(input) || super.keyPressed(input)
  }

  companion object {
    private const val TEXT_PADDING = 13f
  }

  enum class Type {
    DEFAULT,
    PASSWORD
  }

}
