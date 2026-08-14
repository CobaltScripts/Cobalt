package org.cobalt.module.impl.visual

import net.minecraft.client.Minecraft
import net.minecraft.util.ARGB
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.WorldEvent
import org.cobalt.module.Module
import org.cobalt.module.ModuleCategory
import org.cobalt.ui.component.setting.impl.ModeSetting
import org.cobalt.ui.theme.ThemeManager
import org.cobalt.util.render.GizmoRenderer

object PlayerESP : Module(name = "PlayerESP", category = ModuleCategory.VISUAL) {

  private var espType by ModeSetting(
    name = "ESP Type",
    description = "The type of ESP to use",
    defaultValue = 0,
    options = arrayOf("Box", "Target Beam", "Outline")
  )

  fun shouldOutline(entity: Entity): Boolean {
    if (espType != 2) {
      return false
    }

    val mc = Minecraft.getInstance()
    val localPlayer = mc.player ?: return false

    return entity is Player && entity != localPlayer
  }

  fun getOutlineColor(): Int {
    val color = ThemeManager.activeTheme.accentPrimary

    return ARGB.color(
      color.alpha,
      color.red,
      color.green,
      color.blue
    )
  }

  @SubscribeEvent
  fun onWorldRender(ignored: WorldEvent.BeforeGizmos) {
    if (espType == 2) {
      return
    }

    minecraft.level?.players()?.forEach { player ->
      if (player.gameProfile.name == minecraft.gameProfile.name) {
        return@forEach
      }

      when (espType) {
        0 -> GizmoRenderer.drawEntityOutline(
          entity = player,
          color = ThemeManager.activeTheme.accentPrimary,
          esp = true
        )

        1 -> GizmoRenderer.drawTargetBeam(
          entity = player,
          color = ThemeManager.activeTheme.accentPrimary,
          esp = true
        )
      }
    }
  }

}

