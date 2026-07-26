package org.cobalt.mixin.render;

import net.minecraft.client.renderer.LevelRenderer;
import org.cobalt.event.EventBus;
import org.cobalt.event.impl.WorldEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

  @Inject(method = "submitFeatures", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;finalizeGizmoCollection()V"))
  private void cobalt$beforeCollectGizmos(CallbackInfo ci) {
    WorldEvent.BeforeGizmos event = new WorldEvent.BeforeGizmos();
    EventBus.post(event);
  }

}
