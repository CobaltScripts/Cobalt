package org.cobalt.mixin.network;

import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import org.cobalt.api.event.impl.client.ChatEvent;
import org.cobalt.api.event.impl.client.PacketEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class PacketEvent_ClientConnectionMixin {

  @Shadow
  private static <T extends PacketListener> void handlePacket(Packet<T> packet, PacketListener listener) {
  }

  @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
  private static void onPacketReceived(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
    if (packet instanceof BundleS2CPacket bundlePacket) {
      ci.cancel();

      for (Packet<?> subPacket : bundlePacket.getPackets()) {
        handlePacket(subPacket, listener);
      }

      return;
    }

    new PacketEvent.Incoming(packet).post();

    if (packet instanceof GameMessageS2CPacket) {
      new ChatEvent.Receive(packet).post();
    }
  }

  @Inject(method = "sendImmediately", at = @At("HEAD"))
  private void onPacketSent(Packet<?> packet, ChannelFutureListener listener, boolean flush, CallbackInfo ci) {
    new PacketEvent.Outgoing(packet).post();

    if (packet instanceof ChatMessageC2SPacket) {
      new ChatEvent.Send(packet).post();
    }
  }

}
