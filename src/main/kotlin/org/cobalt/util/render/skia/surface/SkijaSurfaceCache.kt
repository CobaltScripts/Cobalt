package org.cobalt.util.render.skia.surface

import io.github.humbleui.skija.BackendRenderTarget
import io.github.humbleui.skija.DirectContext
import io.github.humbleui.skija.Surface

/** Owns the DirectContext/BackendRenderTarget/Surface triple every SkiaSurface backend caches, and their teardown. */
internal class SkijaSurfaceCache {

  var context: DirectContext? = null
  var renderTarget: BackendRenderTarget? = null
    private set
  var surface: Surface? = null
    private set

  fun replaceSurface(renderTarget: BackendRenderTarget, surface: Surface) {
    this.surface?.close()
    this.renderTarget?.close()
    this.renderTarget = renderTarget
    this.surface = surface
  }

  fun close() {
    surface?.close()
    surface = null
    renderTarget?.close()
    renderTarget = null
    context?.close()
    context = null
  }

}
