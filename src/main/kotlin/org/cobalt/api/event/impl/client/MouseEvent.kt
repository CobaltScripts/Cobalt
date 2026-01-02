package org.cobalt.api.event.impl.client

import org.cobalt.api.event.Event

@Suppress("UNUSED_PARAMETER")
abstract class MouseEvent(val button: Int, val action: Int) : Event(true) {

  class LeftClick(action: Int) : MouseEvent(0, action)
  class RightClick(action: Int) : MouseEvent(1, action)
  class MiddleClick(action: Int) : MouseEvent(2, action)

  class LeftRelease(action: Int) : MouseEvent(0, action)
  class RightRelease(action: Int) : MouseEvent(1, action)
  class MiddleRelease(action: Int) : MouseEvent(2, action)

  companion object {
    const val PRESS = 1
    const val RELEASE = 0
  }

}
