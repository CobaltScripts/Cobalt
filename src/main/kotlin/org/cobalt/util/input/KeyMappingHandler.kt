package org.cobalt.util.input

import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.resources.Identifier
import org.cobalt.Cobalt
import org.cobalt.Cobalt.minecraft
import org.cobalt.event.EventBus
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.TickEvent
import org.cobalt.module.ModuleManager
import org.cobalt.ui.screen.ConfigScreen
import org.cobalt.util.scheduling.TickScheduler

object KeyMappingHandler {

  private val category = KeyMapping.Category.register(
    Identifier.fromNamespaceAndPath(Cobalt.MOD_ID, "name")
  )

  private val openGui = KeyMapping(
    "key.cobalt.open_gui",
    InputConstants.Type.KEYSYM,
    InputConstants.KEY_O,
    category
  )

  private val toggleScript = KeyMapping(
    "key.cobalt.toggle_script",
    InputConstants.Type.KEYSYM,
    InputConstants.KEY_P,
    category
  )

  internal fun registerKeyMappings() {
    KeyMappingHelper.registerKeyMapping(openGui)
    KeyMappingHelper.registerKeyMapping(toggleScript)

    EventBus.register(this)
  }

  @SubscribeEvent
  fun onTick(ignored: TickEvent.End) {
    if (minecraft.player == null) {
      return
    }

    if (openGui.consumeClick()) {
      TickScheduler.schedule(1L) {
        minecraft.gui.setScreen(ConfigScreen)
      }
    } else if (toggleScript.consumeClick()) {
      if (ModuleManager.isScriptRunning()) {
        ModuleManager.stopScript()
      } else {
        val lastScript = ModuleManager.lastScript ?: return
        ModuleManager.startScript(lastScript)
      }
    }
  }

}
