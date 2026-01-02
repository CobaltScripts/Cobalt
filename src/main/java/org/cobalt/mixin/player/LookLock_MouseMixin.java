package org.cobalt.mixin.player;

import net.minecraft.client.Mouse;
import org.cobalt.api.util.player.MovementManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class LookLock_MouseMixin {

  @Inject(method = "updateMouse", at = @At("HEAD"), cancellable = true)
  private void onUpdateMouse(CallbackInfo ci) {
    if (MovementManager.isLookLocked) {
      ci.cancel();
    }
  }

}
