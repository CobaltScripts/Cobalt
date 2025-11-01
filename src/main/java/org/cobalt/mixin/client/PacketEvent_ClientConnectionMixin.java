package org.cobalt.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import org.cobalt.api.event.impl.PacketEvent;
import org.cobalt.api.event.impl.ChatEvent;
import org.cobalt.api.event.EventBus;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import io.netty.channel.ChannelFutureListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class PacketEvent_ClientConnectionMixin {

    @Inject(method = "Lnet/minecraft/network/ClientConnection;handlePacket(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;)V", at = @At("HEAD"), cancellable = true)
    private static void onPacketReceived(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
        if (listener instanceof net.minecraft.client.network.ClientPlayNetworkHandler) {
            new PacketEvent.Incoming(packet).post();
            if (packet.getClass().getSimpleName().equals("GameMessageS2CPacket")) {
                new ChatEvent.Receive(packet).post();
            }
        }
    }
    @Inject(method = "Lnet/minecraft/network/ClientConnection;sendImmediately(Lnet/minecraft/network/packet/Packet;Lio/netty/channel/ChannelFutureListener;Z)V", at = @At("HEAD"), cancellable = true)
    private void onPacketSent(Packet<?> packet, ChannelFutureListener listener, boolean flush, CallbackInfo ci) {
        new PacketEvent.Outgoing(packet).post();
        if (packet.getClass().getSimpleName().equals("ChatMessageC2SPacket")) {
            new ChatEvent.Send(packet).post();
        }
    }
}