package org.cobalt.event.impl

import org.cobalt.event.Event

interface TickEvent : Event {

  class Start : TickEvent
  class End : TickEvent

}
