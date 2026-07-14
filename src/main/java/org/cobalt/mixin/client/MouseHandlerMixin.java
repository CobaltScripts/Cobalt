package org.cobalt.mixin.client;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.cobalt.event.EventBus;
import org.cobalt.event.impl.MouseEvent;
import org.cobalt.event.impl.MouseScrollEvent;
import org.cobalt.util.input.Mouse;
import org.cobalt.util.input.MouseAction;
import org.cobalt.util.input.MouseButton;
import org.cobalt.util.input.MouseMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

  @Shadow
  private boolean mouseGrabbed;

  @Shadow
  public abstract void releaseMouse();

  @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
  private void onMouseButton(long handle, MouseButtonInfo rawButtonInfo, int action, CallbackInfo callbackInfo) {
    MouseEvent event = cobalt$createMouseEvent(rawButtonInfo.button(), action);

    if (event == null) {
      return;
    }

    EventBus.post(event);

    if (event.isCancelled()) {
      callbackInfo.cancel();
    }
  }

  @Inject(method = "onScroll", at = @At("HEAD"))
  private void onMouseScroll(long handle, double xoffset, double yoffset, CallbackInfo callbackInfo) {
    EventBus.post(new MouseScrollEvent(xoffset, yoffset));
  }

  @Unique
  private MouseEvent cobalt$createMouseEvent(int button, int action) {
    MouseButton mappedButton = switch (button) {
      case 0 -> MouseButton.LEFT;
      case 1 -> MouseButton.RIGHT;
      case 2 -> MouseButton.MIDDLE;
      default -> null;
    };

    if (mappedButton == null) {
      return null;
    }

    MouseAction mappedAction = switch (action) {
      case 1 -> MouseAction.PRESS;
      case 0 -> MouseAction.RELEASE;
      default -> null;
    };

    if (mappedAction == null) {
      return null;
    }

    return new MouseEvent(mappedButton, mappedAction);
  }

  @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
  private void onUpdateMouse(CallbackInfo callbackInfo) {
    if (Mouse.getMouseMode() == MouseMode.LOCK_MOUSE) {
      callbackInfo.cancel();
    }
  }

  @Inject(method = "isMouseGrabbed", at = @At("HEAD"), cancellable = true)
  private void onIsCursorLocked(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
    if (Mouse.getMouseMode() == MouseMode.UNGRAB_MOUSE) {
      if (this.mouseGrabbed) {
        this.releaseMouse();
      }

      callbackInfoReturnable.setReturnValue(false);
    }
  }

  @Inject(method = "grabMouse", at = @At("HEAD"), cancellable = true)
  private void onLockCursor(CallbackInfo callbackInfo) {
    if (Mouse.getMouseMode() == MouseMode.UNGRAB_MOUSE) {
      callbackInfo.cancel();
    }
  }

}
