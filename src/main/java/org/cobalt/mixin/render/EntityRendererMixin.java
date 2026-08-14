package org.cobalt.mixin.render;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.cobalt.module.impl.visual.PlayerESP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

  @Inject(method = "extractRenderState", at = @At("TAIL"))
  private void cobalt$setOutline(
    Entity entity,
    EntityRenderState state,
    float partialTicks,
    CallbackInfo ci
  ) {
    if (!PlayerESP.INSTANCE.shouldOutline(entity)) {
      return;
    }

    state.outlineColor = PlayerESP.INSTANCE.getOutlineColor();
  }

}
