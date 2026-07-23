package org.cobalt.mixin.gui;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.cobalt.event.EventBus;
import org.cobalt.event.impl.GuiEvent;
import org.cobalt.event.impl.RenderEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

  @Shadow
  private Screen screen;

  @Inject(
    method = "extractRenderState",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V",
      ordinal = 3
    )
  )
  private void cobalt$beforeFinalPop(
    DeltaTracker deltaTracker,
    boolean shouldRenderLevel,
    boolean resourcesLoaded,
    CallbackInfo ci,
    @Local(name = "graphics") GuiGraphicsExtractor graphics
  ) {
    RenderEvent.Notification event = new RenderEvent.Notification(graphics, deltaTracker);
    EventBus.post(event);
  }

  @Inject(
    method = "setScreen",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/client/gui/screens/Screen;added()V",
      shift = At.Shift.AFTER
    )
  )
  private void cobalt$onScreenOpen(Screen screen, CallbackInfo ci) {
    GuiEvent.Open event = new GuiEvent.Open(screen);
    EventBus.post(event);
  }

  @Inject(
    method = "setScreen",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/client/gui/screens/Screen;removed()V"
    )
  )
  private void cobalt$onCloseScreen(Screen screen, CallbackInfo ci) {
    GuiEvent.Close event = new GuiEvent.Close(this.screen);
    EventBus.post(event);
  }

  @Inject(
    method = "extractRenderState",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V",
      ordinal = 3,
      shift = At.Shift.AFTER
    )
  )
  private void cobalt$renderScreen(
    DeltaTracker deltaTracker,
    boolean shouldRenderLevel,
    boolean resourcesLoaded,
    CallbackInfo ci
  ) {
    GuiEvent.Draw event = new GuiEvent.Draw();
    EventBus.post(event);
  }

}
