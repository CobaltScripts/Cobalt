package org.cobalt.internal.helper

import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderSetup

internal object RenderLayers {

  // this is kinda ugly, subject to change
  private val LINE_LIST_SETUP: RenderSetup =
    RenderSetup.builder(RenderPipelines.LINE_LIST).build()

  private val LINE_LIST_ESP_SETUP: RenderSetup =
    RenderSetup.builder(RenderPipelines.LINE_LIST_ESP).build()

  private val TRIANGLE_STRIP_SETUP: RenderSetup =
    RenderSetup.builder(RenderPipelines.TRIANGLE_STRIP).build()

  private val TRIANGLE_STRIP_ESP_SETUP: RenderSetup =
    RenderSetup.builder(RenderPipelines.TRIANGLE_STRIP_ESP).build()

  val LINE_LIST: RenderLayer = RenderLayer.of("line-list", LINE_LIST_SETUP)

  val LINE_LIST_ESP: RenderLayer = RenderLayer.of("line-list-esp", LINE_LIST_ESP_SETUP)

  val TRIANGLE_STRIP: RenderLayer = RenderLayer.of("triangle_strip", TRIANGLE_STRIP_SETUP)

  val TRIANGLE_STRIP_ESP: RenderLayer = RenderLayer.of("triangle_strip_esp", TRIANGLE_STRIP_ESP_SETUP)
}
