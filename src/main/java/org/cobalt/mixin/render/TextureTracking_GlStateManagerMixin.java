package org.cobalt.mixin.render;

import com.mojang.blaze3d.opengl.GlStateManager;
import org.cobalt.api.util.ui.TextureTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Implementation from vexel by StellariumMC
 * Original work: <a href="https://github.com/StellariumMC/vexel">...</a>
 *
 * @author StellariumMC
 */
@Mixin(GlStateManager.class)
public class TextureTracking_GlStateManagerMixin {

  @Inject(method = "_bindTexture", at = @At("HEAD"), remap = false)
  private static void onBindTexture(int texture, CallbackInfo ci) {
    TextureTracker.setPrevBoundTexture(texture);
  }

  @Inject(method = "_activeTexture", at = @At("HEAD"), remap = false)
  private static void onActiveTexture(int texture, CallbackInfo ci) {
    TextureTracker.setPrevActiveTexture(texture);
  }

}
