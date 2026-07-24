package org.cobalt.module

import net.minecraft.client.gui.screens.LevelLoadingScreen
import net.minecraft.client.gui.screens.ProgressScreen
import org.cobalt.Cobalt.minecraft
import org.cobalt.event.EventBus
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.RenderEvent
import org.cobalt.module.impl.combat.AutoClickerModule
import org.cobalt.module.impl.misc.AutoHarpModule
import org.cobalt.module.impl.misc.AutoSprintModule
import org.cobalt.module.impl.misc.DebugModule
import org.cobalt.module.impl.misc.DiscordRPCModule
import org.cobalt.module.impl.misc.NickHiderModule
import org.cobalt.module.impl.misc.RotationsModule
import org.cobalt.module.impl.script.fishing.FishingScript
import org.cobalt.module.impl.visual.PerformanceHUDModule
import org.cobalt.module.type.RenderableModule
import org.cobalt.module.type.Script
import org.cobalt.ui.screen.HudEditorScreen
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.client.WindowUtils.scaleX
import org.cobalt.util.client.WindowUtils.scaleY
import org.cobalt.util.render.SkiaRenderer
import org.cobalt.util.render.skia.SkiaPIP

object ModuleManager {

  val modules = mutableSetOf<Module>()
  var currentScript: Script? = null

  init {
    EventBus.register(this)
  }

  internal fun registerModules() {
    val builtIn = arrayOf(
      AutoClickerModule,
      AutoHarpModule,
      AutoSprintModule,
      DebugModule,
      DiscordRPCModule,
      NickHiderModule,
      RotationsModule,
      FishingScript,
      PerformanceHUDModule,
    )

    builtIn.forEach { module ->
      addModule(module)
    }
  }

  fun addModule(module: Module) {
    if (!modules.add(module)) {
      error("'${module.name}' is already registered")
    }

    module.loadConfig()
    module.onRegistration()
  }

  fun getModule(moduleName: String): Module? {
    return modules.find { module ->
      module.name.equals(moduleName, true)
    }
  }

  fun startScript(script: Script) {
    if (currentScript != null && currentScript != script) {
      stopAllScripts()
      ChatUtils.sendSystemMessage(
        "<red>Cannot start a different script when one is currently active, disabling all scripts...</red>"
      )

      return
    }

    currentScript = script
    script.startScript()
  }

  fun stopScript() {
    if (currentScript == null) {
      ChatUtils.sendSystemMessage("<red>There is no script currently running</red>")
      return
    }

    currentScript?.stopScript().also {
      currentScript = null
    }
  }

  fun stopAllScripts() {
    modules
      .filterIsInstance<Script>()
      .forEach { script ->
        script.stopScript()
      }

    currentScript = null
  }

  fun getScript(scriptName: String): Script? {
    return modules
      .filterIsInstance<Script>()
      .find { script ->
        script.name.equals(scriptName, true)
      }
  }

  private val shouldSkipRender: Boolean
    get() {
      return minecraft.level == null ||
        minecraft.player == null ||
        minecraft.gameRenderer.gameRenderState().guiRenderState.isHudHidden ||
        minecraft.debugOverlay.showDebugScreen() ||
        minecraft.gui.screen() is LevelLoadingScreen ||
        minecraft.gui.screen() is ProgressScreen ||
        minecraft.gui.screen() is HudEditorScreen
    }

  @SubscribeEvent
  fun onHudRender(event: RenderEvent.Hud) {
    if (shouldSkipRender) {
      return
    }

    SkiaPIP.drawSkia(event.graphics) {
      modules.filterIsInstance<RenderableModule>()
        .filter { module -> module.enabled }
        .forEach { module ->
          SkiaRenderer.push()

          val renderX = module.xPos * scaleX
          val renderY = module.yPos * scaleY
          val finalScale = module.scale * scaleY

          SkiaRenderer.translate(renderX, renderY)
          SkiaRenderer.scale(finalScale, finalScale)
          SkiaRenderer.translate(-module.xPos, -module.yPos)

          module.renderComponent()

          SkiaRenderer.pop()
        }
    }
  }

}


