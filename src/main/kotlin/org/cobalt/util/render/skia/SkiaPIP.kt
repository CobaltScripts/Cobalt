package org.cobalt.util.render.skia

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState
import org.cobalt.Cobalt.minecraft
import org.cobalt.util.render.skia.surface.SkiaSurface
import org.joml.Matrix3x2f

class SkiaPIP : PictureInPictureRenderer<SkiaPIP.SkiaRenderState>() {

  private val surface = SkiaSurface.getInstance()

  override fun getTranslateY(height: Int, guiScale: Int) = height / 2f
  override fun getRenderStateClass() = SkiaRenderState::class.java
  override fun getTextureLabel(): String = "Skia"

  override fun renderToTexture(state: SkiaRenderState, poseStack: PoseStack, submitNodeCollector: SubmitNodeCollector) {
    val colorView = RenderSystem.outputColorTextureOverride ?: return
    val width = colorView.getWidth(0).takeIf { it > 0 } ?: return
    val height = colorView.getHeight(0).takeIf { it > 0 } ?: return

    surface.render(
      width, height,
      colorView.texture()
    ) {
      state.runnable.run()
    }
  }

  override fun close() {
    surface.close()
    super.close()
  }

  data class SkiaRenderState(
    private val width: Int,
    private val height: Int,
    val poseMatrix: Matrix3x2f,
    private val scissor: ScreenRectangle?,
    private val bounds: ScreenRectangle,
    val runnable: Runnable,
  ) : PictureInPictureRenderState {
    override fun x0() = 0
    override fun y0() = 0
    override fun x1() = width
    override fun y1() = height
    override fun scissorArea() = scissor
    override fun bounds() = bounds
    override fun scale() = 1f
    override fun pose() = poseMatrix
  }

  companion object {

    @JvmStatic
    fun drawSkia(graphics: GuiGraphicsExtractor, runnable: Runnable) {
      val window = minecraft.window
      val width = window.width
      val height = window.height

      val scissor = graphics.scissorStack.peek()
      val pose = Matrix3x2f(graphics.pose())

      val screenRect = ScreenRectangle(0, 0, width, height).transformMaxBounds(pose)

      if (screenRect.width <= 0 || screenRect.height <= 0) {
        return
      }

      val bounds = scissor?.intersection(screenRect) ?: screenRect

      if (bounds.width <= 0 || bounds.height <= 0) {
        return
      }

      val state = SkiaRenderState(
        width = width,
        height = height,
        poseMatrix = pose,
        scissor = scissor,
        bounds = bounds,
        runnable = runnable
      )

      graphics.guiRenderState.addPicturesInPictureState(state)
    }

  }

}
