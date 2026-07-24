package org.cobalt.event.impl

import org.cobalt.event.Event

abstract class WorldEvent : Event() {

  class GizmoRender : WorldEvent()
  class Load : WorldEvent()

}
