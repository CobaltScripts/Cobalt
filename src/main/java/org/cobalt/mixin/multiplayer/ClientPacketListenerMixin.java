package org.cobalt.mixin.multiplayer;

import net.minecraft.client.multiplayer.ClientPacketListener;
import org.cobalt.command.CommandManager;
import org.cobalt.event.EventBus;
import org.cobalt.event.impl.ChatSendEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

  @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
  public void cobalt$sendChatMessage(String content, CallbackInfo callbackInfo) {
    if (CommandManager.handleCommandExecution$cobalt(content)) {
      callbackInfo.cancel();
      return;
    }

    ChatSendEvent event = new ChatSendEvent(content);
    EventBus.post(event);

    if (event.isCancelled()) {
      callbackInfo.cancel();
    }
  }

}
