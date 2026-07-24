package org.cobalt.event.impl

import net.minecraft.client.gui.screens.Screen
import org.cobalt.event.Event

interface GuiEvent : Event {

  class Open(val screen: Screen) : GuiEvent
  class Draw(val screen: Screen) : GuiEvent
  class Close(val screen: Screen) : GuiEvent

}
