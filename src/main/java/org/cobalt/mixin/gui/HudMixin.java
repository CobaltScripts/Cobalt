package org.cobalt.mixin.gui;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.cobalt.event.EventBus;
import org.cobalt.event.impl.RenderEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class HudMixin {

  @Inject(method = "extractRenderState", at = @At("TAIL"))
  public void cobalt$extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
    RenderEvent.Hud event = new RenderEvent.Hud(graphics, deltaTracker);
    EventBus.post(event);
  }

}
