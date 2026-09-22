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
    options = arrayOf("Box", "Target Beam", "Outline", "Beam & Outline")
  )

  private enum class EspType(val optionIndex: Int) {
    BOX(0),
    TARGET_BEAM(1),
    OUTLINE(2),
    BEAM_AND_OUTLINE(3);

    companion object {
      fun fromOptionIndex(index: Int): EspType = entries.firstOrNull { it.optionIndex == index } ?: BOX
    }
  }

  private val espTypeMode: EspType
    get() = EspType.fromOptionIndex(espType)

  fun shouldOutline(entity: Entity): Boolean {
    if (espTypeMode != EspType.OUTLINE && espTypeMode != EspType.BEAM_AND_OUTLINE) {
      return false
    }

    val mc = Minecraft.getInstance()
    val localPlayer = mc.player ?: return false

    return entity is Player && entity != localPlayer && entity.uuid.version() != 2
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
    if (espTypeMode == EspType.OUTLINE) {
      return
    }

    minecraft.level?.players()?.forEach { player ->
      if (player.gameProfile.name == minecraft.gameProfile.name) {
        return@forEach
      }

      if (player.uuid.version() == 2) return@forEach

      when (espTypeMode) {
        EspType.BOX -> GizmoRenderer.drawEntityOutline(
          entity = player,
          color = ThemeManager.activeTheme.accentPrimary,
          esp = true
        )

        EspType.TARGET_BEAM, EspType.BEAM_AND_OUTLINE -> GizmoRenderer.drawTargetBeam(
          entity = player,
          color = ThemeManager.activeTheme.accentPrimary,
          esp = true
        )

        EspType.OUTLINE -> Unit // handled by shouldOutline(), not drawn in this loop
      }
    }
  }

}

