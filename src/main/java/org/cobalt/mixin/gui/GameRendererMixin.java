package org.cobalt.mixin.gui;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.renderer.GameRenderer;
import org.cobalt.event.EventBus;
import org.cobalt.event.impl.RenderEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

  @Shadow
  @Final
  private Minecraft minecraft;

  @Inject(
    method = "extractGui",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V",
      ordinal = 0
    )
  )
  private void cobalt$renderHud(
    DeltaTracker deltaTracker,
    boolean shouldRenderLevel,
    boolean resourcesLoaded,
    CallbackInfo ci,
    @Local(name = "graphics") GuiGraphicsExtractor graphics
  ) {
    if (!(this.minecraft.screen instanceof LevelLoadingScreen)) {
      EventBus.post(new RenderEvent.Hud(graphics, deltaTracker));
    }
  }

  @Inject(
    method = "extractGui",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/client/gui/Gui;extractSavingIndicator(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
      shift = At.Shift.AFTER
    )
  )
  private void cobalt$renderNotifications(
    DeltaTracker deltaTracker,
    boolean shouldRenderLevel,
    boolean resourcesLoaded,
    CallbackInfo ci,
    @Local(name = "graphics") GuiGraphicsExtractor graphics
  ) {
    EventBus.post(new RenderEvent.Notification(graphics, deltaTracker));
  }

}
