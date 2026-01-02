package org.cobalt.api.event.impl.render

import net.minecraft.client.render.Camera
import net.minecraft.client.render.Frustum
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import org.cobalt.api.event.Event

@Suppress("UNUSED_PARAMETER")
abstract class WorldRenderEvent(val context: WorldRenderContext) : Event() {
  class Start(context: WorldRenderContext) : WorldRenderEvent(context)
  class Last(context: WorldRenderContext) : WorldRenderEvent(context)
}

class WorldRenderContext {
  var matrixStack: MatrixStack? = null
  lateinit var consumers: VertexConsumerProvider
  lateinit var camera: Camera
  lateinit var frustum: Frustum
}
