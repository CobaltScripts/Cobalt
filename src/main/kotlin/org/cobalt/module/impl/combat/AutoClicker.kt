package org.cobalt.module.impl.combat

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.WorldEvent
import org.cobalt.module.Module
import org.cobalt.module.ModuleCategory
import org.cobalt.ui.component.setting.impl.KeyBindSetting
import org.cobalt.ui.component.setting.impl.ModeSetting
import org.cobalt.ui.component.setting.impl.SliderSetting
import org.cobalt.util.input.Keyboard
import org.cobalt.util.input.Mouse
import org.cobalt.util.scheduling.Clock

object AutoClicker : Module(
  name = "AutoClicker",
  category = ModuleCategory.COMBAT
) {

  private val leftClickKeybind by KeyBindSetting(
    name = "Left Click",
    description = "Hold to automatically left click",
    defaultValue = InputConstants.UNKNOWN,
  )

  private val rightClickKeybind by KeyBindSetting(
    name = "Right Click",
    description = "Hold to automatically right click",
    defaultValue = InputConstants.UNKNOWN,
  )

  private val leftClickCps by SliderSetting(
    name = "Left CPS",
    description = "CPS for the left click",
    defaultValue = 6,
    min = 1,
    max = 20,
  )

  private val rightClickCps by SliderSetting(
    name = "Right CPS",
    description = "CPS for the right click",
    defaultValue = 6,
    min = 1,
    max = 20,
  )

  private val attackMode by ModeSetting(
    name = "Attack Mode",
    description = "Controls when left clicks are performed",
    defaultValue = 0,
    options = arrayOf("All", "Entity Only", "No Blocks"),
  )

  private val mobFilter by TextSetting(
    name = "Mob Filter",
    description = "Only attack listed names (comma-separated)",
    defaultValue = "",
    placeholder = "Enter mob name..."
  )

  private val waitForReload by CheckboxSetting(
    name = "Wait For Reload",
    description = "Wait until the attack cooldown is ready",
    defaultValue = false
  )

  private val leftClickDelay = Clock()
  private val rightClickDelay = Clock()

  @SubscribeEvent
  fun onRender(ignored: WorldEvent.Render) {
    if (!enabled) {
      return
    }

    if (minecraft.gui.screen() != null) {
      return
    }

    if (
      leftClickDelay.passed() &&
      Keyboard.isKeyDown(leftClickKeybind) &&
      canLeftClick()
    ) {
      Mouse.leftClick()
      leftClickDelay.schedule(nextDelay(leftClickCps))
    }

    if (
      rightClickDelay.passed() &&
      Keyboard.isKeyDown(rightClickKeybind)
    ) {
      Keyboard.press(minecraft.options.keyUse)
      rightClickDelay.schedule(nextDelay(rightClickCps))
    }
  }

  private fun canLeftClick(): Boolean {
    val player = minecraft.player ?: return false
    val hit = minecraft.hitResult

    val attackAllowed = when (attackMode) {
      0 -> true
      1 -> hit?.type == HitResult.Type.ENTITY
      2 -> hit?.type != HitResult.Type.BLOCK
      else -> false
    }

    if (!attackAllowed || (waitForReload && player.getAttackStrengthScale(0f) < 1.0f)) {
      return false
    }

    if (mobFilter.isBlank()) {
      return true
    }

    val entity = (hit as? EntityHitResult)?.entity ?: return false
    val entityName = entity.name.string.lowercase()

    return mobFilter
      .split(",")
      .map { it.trim().lowercase() }
      .any { name -> entityName.contains(name) }
  }

  private fun nextDelay(cps: Int): Long {
    val base = 1000.0 / cps
    val jitter = (Math.random() - 0.5) * 60.0
    return (base + jitter).coerceAtLeast(1.0).toLong()
  }

}
