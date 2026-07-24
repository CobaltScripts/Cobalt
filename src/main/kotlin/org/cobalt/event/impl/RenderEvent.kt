package org.cobalt.event.impl

import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphicsExtractor
import org.cobalt.event.Event

abstract class RenderEvent(
  val graphics: GuiGraphicsExtractor,
  val deltaTracker: DeltaTracker,
) : Event {

  class Hud(
    graphics: GuiGraphicsExtractor,
    deltaTracker: DeltaTracker,
  ) : RenderEvent(graphics, deltaTracker)

  class Notification(
    graphics: GuiGraphicsExtractor,
    deltaTracker: DeltaTracker,
  ) : RenderEvent(graphics, deltaTracker)

}
