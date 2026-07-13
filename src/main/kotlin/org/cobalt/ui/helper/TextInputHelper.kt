package org.cobalt.ui.helper

import java.util.function.Consumer
import kotlin.math.abs
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import org.cobalt.ui.component.TextInputComponent
import org.cobalt.util.input.Mouse
import org.cobalt.util.render.SkiaRenderer
import org.lwjgl.glfw.GLFW

class TextInputHelper(
  private val fontSize: Float,
  private val textInputType: TextInputComponent.Type,
  private val onChange: Consumer<String>,
) {

  var text = ""
    set(value) {
      if (field == value) {
        return
      }

      onChange.accept(value)
      field = value
    }

  var focused = false
    private set

  var caretIndex = text.length
    private set

  var selectionStart = -1
    private set

  var selectionEnd = -1
    private set

  var selectionAnchor = -1
    private set

  val hasSelection: Boolean
    get() = selectionStart != -1 && selectionStart != selectionEnd

  val usePlaceholder: Boolean
    get() = text.isBlank() && !focused

  private var xPos: Float = 0f
  private var yPos: Float = 0f
  private var width: Float = 0f
  private var height: Float = 0f

  fun updateBounds(xPos: Float, yPos: Float, width: Float, height: Float) {
    this.xPos = xPos
    this.yPos = yPos
    this.width = width
    this.height = height
  }

  fun clearSelection() {
    selectionStart = -1
    selectionEnd = -1
    selectionAnchor = -1
  }

  private fun updateSelectionRange() {
    if (selectionAnchor != -1 && selectionAnchor != caretIndex) {
      selectionStart = minOf(selectionAnchor, caretIndex)
      selectionEnd = maxOf(selectionAnchor, caretIndex)
    } else {
      selectionStart = -1
      selectionEnd = -1
    }
  }

  fun mouseClicked(button: Int, relativeX: Float): Boolean {
    if (button != 0) {
      return false
    }

    val hoveringOver = Mouse.isHoveringOver(xPos, yPos, width, height)
    focused = hoveringOver

    if (hoveringOver) {
      caretIndex = calculateCaretIndex(relativeX)
      selectionAnchor = caretIndex
      clearSelection()
    }

    return hoveringOver
  }

  fun mouseDragged(button: Int, relativeX: Float): Boolean {
    if (button != 0 || !focused) {
      return false
    }

    if (selectionAnchor == -1) {
      selectionAnchor = caretIndex
    }

    caretIndex = calculateCaretIndex(relativeX)
    updateSelectionRange()
    return true
  }

  fun mouseReleased(button: Int): Boolean {
    if (button != 0) {
      return false
    }

    focused = Mouse.isHoveringOver(xPos, yPos, width, height)
    return focused
  }

  private fun calculateCaretIndex(relativeX: Float): Int {
    if (text.isEmpty()) {
      return 0
    }

    var bestIndex = 0
    var minDiff = Float.MAX_VALUE

    for (i in 0..text.length) {
      val prefix = if (textInputType == TextInputComponent.Type.PASSWORD) "*".repeat(i) else text.substring(0, i)
      val w = SkiaRenderer.textWidth(SkiaRenderer.regularFont, prefix, fontSize)
      val diff = abs(w - relativeX)

      if (diff < minDiff) {
        minDiff = diff
        bestIndex = i
      }
    }

    return bestIndex
  }

  fun charTyped(input: CharacterEvent): Boolean {
    if (focused && input.isAllowedChatCharacter) {
      val str = input.codepointAsString()

      if (hasSelection) {
        text = text.substring(0, selectionStart) + str + text.substring(selectionEnd)
        caretIndex = selectionStart + str.length
        clearSelection()
      } else {
        text = text.substring(0, caretIndex) + str + text.substring(caretIndex)
        caretIndex += str.length
      }

      return true
    }

    return false
  }

  fun keyPressed(input: KeyEvent): Boolean {
    if (!focused) {
      return false
    }

    val isShiftDown = (input.modifiers() and GLFW.GLFW_MOD_SHIFT) != 0
    val isCtrlDown = (input.modifiers() and GLFW.GLFW_MOD_CONTROL) != 0

    return when (input.key()) {
      GLFW.GLFW_KEY_A -> {
        if (!isCtrlDown) {
          return false
        }

        selectionAnchor = 0
        selectionStart = 0
        selectionEnd = text.length
        caretIndex = text.length
        true
      }

      GLFW.GLFW_KEY_BACKSPACE -> {
        if (hasSelection) {
          text = text.substring(0, selectionStart) + text.substring(selectionEnd)
          caretIndex = selectionStart
          clearSelection()
        } else if (caretIndex > 0) {
          text = text.substring(0, caretIndex - 1) + text.substring(caretIndex)
          caretIndex--
        }

        true
      }

      GLFW.GLFW_KEY_LEFT -> handleMoveLeft(isShiftDown)
      GLFW.GLFW_KEY_RIGHT -> handleMoveRight(isShiftDown)
      else -> false
    }
  }

  private fun handleMoveLeft(isShiftDown: Boolean): Boolean {
    if (isShiftDown) {
      if (selectionAnchor == -1) selectionAnchor = caretIndex
      if (caretIndex > 0) {
        caretIndex--
        updateSelectionRange()
      }
    } else {
      if (hasSelection) {
        caretIndex = selectionStart
        clearSelection()
      } else if (caretIndex > 0) {
        caretIndex--
      }
    }

    return true
  }

  private fun handleMoveRight(isShiftDown: Boolean): Boolean {
    if (isShiftDown) {
      if (selectionAnchor == -1) selectionAnchor = caretIndex
      if (caretIndex < text.length) {
        caretIndex++
        updateSelectionRange()
      }
    } else {
      if (hasSelection) {
        caretIndex = selectionEnd
        clearSelection()
      } else if (caretIndex < text.length) {
        caretIndex++
      }
    }

    return true
  }

}
