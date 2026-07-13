package org.cobalt.event.impl

import org.cobalt.event.Event
import org.cobalt.util.input.MouseAction
import org.cobalt.util.input.MouseButton

class MouseEvent(
  val button: MouseButton,
  val action: MouseAction,
) : Event.Cancellable()
