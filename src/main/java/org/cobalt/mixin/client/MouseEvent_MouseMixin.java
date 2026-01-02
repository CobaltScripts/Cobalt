package org.cobalt.mixin.client;

import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;
import org.cobalt.api.event.impl.client.MouseEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseEvent_MouseMixin {

  @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
  private void onMouseButton(long window, MouseInput input, int action, CallbackInfo ci) {
    MouseEvent event;
    int button = input.button();
    boolean isDown = action == 1;

    if (isDown) {
      switch (button) {
        case 0 -> event = new MouseEvent.LeftClick(button);
        case 1 -> event = new MouseEvent.RightClick(button);
        case 2 -> event = new MouseEvent.MiddleClick(button);

        default -> {
          return;
        }
      }
    } else {
      switch (button) {
        case 0 -> event = new MouseEvent.LeftRelease(button);
        case 1 -> event = new MouseEvent.RightRelease(button);
        case 2 -> event = new MouseEvent.MiddleRelease(button);

        default -> {
          return;
        }
      }
    }

    event.post();

    if (event.isCancelled()) {
      ci.cancel();
    }
  }

}
