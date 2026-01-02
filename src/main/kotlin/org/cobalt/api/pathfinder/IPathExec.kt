package org.cobalt.api.pathfinder

import net.minecraft.client.network.ClientPlayerEntity
import org.cobalt.api.event.impl.render.WorldRenderContext

interface IPathExec {

  fun onTick(player: ClientPlayerEntity) {}
  fun onWorldRenderLast(ctx: WorldRenderContext, player: ClientPlayerEntity) {}
}
