package org.cobalt.event.impl

import org.cobalt.event.Event

class MouseScrollEvent(
  val horizontalAmount: Double,
  val verticalAmount: Double,
) : Event
