package org.cobalt.mixin.client;

import net.minecraft.client.MinecraftClient;
import org.cobalt.api.event.impl.client.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class TickEvent_MinecraftClientMixin {

  @Inject(at = @At("HEAD"), method = "tick")
  private void onStartTick(CallbackInfo info) {
    new TickEvent.Start().post();
  }

  @Inject(at = @At("RETURN"), method = "tick")
  private void onEndTick(CallbackInfo info) {
    new TickEvent.End().post();
  }

}
