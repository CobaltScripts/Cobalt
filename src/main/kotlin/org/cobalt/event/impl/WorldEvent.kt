package org.cobalt.event.impl

import org.cobalt.event.Event

interface WorldEvent : Event {

  class BeforeGizmos : WorldEvent
  class Change : WorldEvent

}
