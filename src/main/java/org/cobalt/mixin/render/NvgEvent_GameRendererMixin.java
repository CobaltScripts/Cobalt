package org.cobalt.mixin.render;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.cobalt.api.event.impl.render.NvgEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class NvgEvent_GameRendererMixin {

  @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;incrementFrame()V", shift = At.Shift.AFTER), cancellable = true)
  public void renderNvg(RenderTickCounter counter, boolean tick, CallbackInfo ci) {
    new NvgEvent().post();
  }

}
