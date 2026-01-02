package org.cobalt.mixin.client;

import net.minecraft.client.MinecraftClient;
import org.cobalt.internal.loader.AddonLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class UnloadAddons_MinecraftClientMixin {

  @Inject(method = "close", at = @At("HEAD"))
  public void onClose(CallbackInfo ci) {
    AddonLoader.INSTANCE.getAddons().forEach((addon) -> {
      addon.getSecond().onUnload();
    });
  }

}
