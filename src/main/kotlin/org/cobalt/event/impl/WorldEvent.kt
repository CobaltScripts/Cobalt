package org.cobalt.event.impl

import org.cobalt.event.Event

interface WorldEvent : Event {

  class GizmoRender : WorldEvent
  class Load : WorldEvent

}
