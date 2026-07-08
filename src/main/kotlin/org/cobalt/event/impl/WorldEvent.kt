package org.cobalt.event.impl

import org.cobalt.event.Event

abstract class WorldEvent : Event() {

  class Render : WorldEvent()
  class Load : WorldEvent()

}
