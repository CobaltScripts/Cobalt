package org.cobalt.module.impl.skills

import kotlin.random.Random
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.TickEvent
import org.cobalt.event.impl.WorldEvent
import org.cobalt.module.ModuleCategory
import org.cobalt.module.impl.misc.Rotations
import org.cobalt.module.type.Script
import org.cobalt.ui.component.setting.impl.TextSetting
import org.cobalt.ui.theme.ThemeManager
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.input.Mouse
import org.cobalt.util.inventory.InventoryUtils
import org.cobalt.util.render.GizmoRenderer
import org.cobalt.util.rotation.RotationMath
import org.cobalt.util.scheduling.Clock

object Fishing : Script(
  name = "Fishing",
  category = ModuleCategory.SKILLS,
  backgroundResourcePath = "/assets/cobalt/ui/scripts/fishing.png"
) {

  val rodName by TextSetting(
    name = "Rod Name",
    description = "Name of rod being used",
    defaultValue = "",
    placeholder = "Enter rod name..."
  )

  private var state = State.HOLD_ROD
  private val delayClock = Clock()
  private val antiAfkDelay = Clock()
  private var lookPos: Vec3? = null

  override fun onEnable() {
    state = State.HOLD_ROD
    antiAfkDelay.schedule(Random.nextLong(10_000, 15_000))

    val player = minecraft.player ?: return
    val start = player.eyePosition
    val end = start.add(player.lookAngle.scale(20.0))

    lookPos = minecraft.level?.clip(
      ClipContext(
        start,
        end,
        ClipContext.Block.OUTLINE,
        ClipContext.Fluid.ANY,
        player
      )
    )?.location

    if (lookPos == null) {
      ChatUtils.sendSystemMessage("<red>You need to be looking at a fluid</red>")
      return
    }

    super.onEnable()
  }

  @SubscribeEvent
  fun onTick(ignored: TickEvent.Start) {
    if (minecraft.level == null || minecraft.player == null) {
      return
    }

    if (!delayClock.passed()) {
      return
    }

    when (state) {
      State.HOLD_ROD -> {
        val held = InventoryUtils.holdItem(rodName)

        if (!held) {
          stopScript()
          return
        }

        delayClock.schedule(Random.nextLong(250, 350))
        state = State.CAST_ROD
      }

      State.CAST_ROD -> {
        if (minecraft.player?.fishing == null) {
          Mouse.rightClick()
        }

        state = State.WAIT_FOR_CATCH
      }

      State.WAIT_FOR_CATCH -> {
        if (antiAfkDelay.passed()) {
          lookPos?.add(
            Random.nextDouble(-0.25, 0.25),
            Random.nextDouble(-0.25, 0.25),
            Random.nextDouble(-0.25, 0.25)
          )?.let {
            Rotations.start(RotationMath.getRotation(it))
          }

          antiAfkDelay.schedule(Random.nextLong(10_000, 15_000))
        }

        if (!detectFishBite()) {
          return
        }

        delayClock.schedule(Random.nextLong(200, 550))
        state = State.REEL_IN
      }

      State.REEL_IN -> {
        Mouse.rightClick()
        delayClock.schedule(Random.nextLong(100, 250))
        state = State.CAST_ROD
      }
    }
  }

  @SubscribeEvent
  fun onRender(ignored: WorldEvent.Render) {
    if (!enabled) {
      return
    }

    lookPos?.let {
      GizmoRenderer.drawBox(
        AABB(
          it.x - 0.25,
          it.y - 0.25,
          it.z - 0.25,
          it.x + 0.25,
          it.y + 0.25,
          it.z + 0.25
        ), ThemeManager.activeTheme.accentPrimary
      )
    }
  }

  fun detectFishBite(): Boolean {
    return minecraft.level
      ?.entitiesForRendering()
      ?.any {
        it is ArmorStand && it.customName?.string == "!!!"
      } ?: false
  }

  enum class State {
    HOLD_ROD,
    CAST_ROD,
    WAIT_FOR_CATCH,
    REEL_IN
  }

}
