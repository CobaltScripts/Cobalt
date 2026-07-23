package org.cobalt.mixin.network;

import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import org.cobalt.event.EventBus;
import org.cobalt.event.impl.PacketEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ConnectionMixin {

  @Inject(method = "genericsFtw", at = @At("HEAD"), cancellable = true)
  private static void cobalt$onPacketReceived(Packet<?> packet, PacketListener listener, CallbackInfo callbackInfo) {
    PacketEvent.Receive event = new PacketEvent.Receive(packet);
    EventBus.post(event);

    if (event.isCancelled()) {
      callbackInfo.cancel();
    }
  }

  @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
  private void cobalt$onPacketSent(Packet<?> packet, ChannelFutureListener listener, boolean flush, CallbackInfo callbackInfo) {
    PacketEvent.Send event = new PacketEvent.Send(packet);
    EventBus.post(event);

    if (event.isCancelled()) {
      callbackInfo.cancel();
    }
  }

}
