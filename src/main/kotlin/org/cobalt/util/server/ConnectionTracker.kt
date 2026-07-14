package org.cobalt.util.server

import kotlin.math.min
import net.minecraft.network.protocol.game.ClientboundSetTimePacket
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket
import net.minecraft.util.Util
import org.cobalt.Cobalt.minecraft
import org.cobalt.event.EventBus
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.PacketEvent

object ConnectionTracker {

  @JvmStatic
  var averageTps = 20.0
    private set

  @JvmStatic
  var currentPing = 0
    private set

  @JvmStatic
  var averagePing = 0
    private set

  private var lastTickTime = -1L

  init {
    EventBus.register(this)
  }

  @SubscribeEvent
  fun onPacketReceive(event: PacketEvent.Receive) {
    when (event.packet) {
      is ClientboundSetTimePacket -> {
        averageTps = (20000.0 / (System.currentTimeMillis() - lastTickTime + 1))
          .coerceIn(0.0, 20.0)

        lastTickTime = System.currentTimeMillis()
      }

      is ClientboundPongResponsePacket -> {
        currentPing = (Util.getMillis() - event.packet.time).toInt().coerceAtLeast(0)

        val pingLog = minecraft.debugOverlay.pingLogger
        val sampleSize = min(pingLog.size(), 20)

        if (sampleSize == 0) {
          averagePing = currentPing
          return
        }

        var total = 0L

        for (i in 0 until sampleSize) {
          total += pingLog.get(i)
        }

        averagePing = (total / sampleSize).toInt()
      }
    }
  }

}
