package org.cobalt.module.impl.script.fishing

import kotlin.random.Random
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.TickEvent
import org.cobalt.event.impl.WorldEvent
import org.cobalt.module.ModuleCategory
import org.cobalt.module.impl.script.ScriptState
import org.cobalt.module.impl.script.fishing.state.CastRodState
import org.cobalt.module.type.Script
import org.cobalt.ui.component.setting.impl.SliderSetting
import org.cobalt.ui.theme.ThemeManager
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.chat.MessageType
import org.cobalt.util.render.GizmoRenderer
import org.cobalt.util.scheduling.Clock

object FishingScript : Script(
  name = "Fishing",
  category = ModuleCategory.SKILLS,
  backgroundResourcePath = "/assets/cobalt/ui/scripts/fishing.png",
) {

  val rodSlot by SliderSetting(
    name = "Rod Slot",
    description = "Hotbar slot of rod",
    defaultValue = 1,
    min = 1,
    max = 9
  )

  var state: ScriptState? = null
  var lookPos: Vec3? = null

  val globalDelay = Clock()
  val antiAfkDelay = Clock()

  override fun onEnable() {
    val player = minecraft.player ?: return
    val start = player.eyePosition
    val end = start.add(player.lookAngle.scale(20.0))

    lookPos = minecraft.level?.clip(
      ClipContext(
        start, end,
        ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, player
      )
    )?.location

    if (lookPos == null) {
      ChatUtils.sendSystemMessage("<red>You need to be looking at a fluid</red>")
      return
    }

    val hook = player.fishing
    val inLiquid = hook != null && (hook.isInWater || hook.isInLava)

    antiAfkDelay.schedule(Random.nextLong(10_000, 15_000))
    globalDelay.schedule(1000)

    changeState(CastRodState(rodSlot, !inLiquid))
    super.onEnable()
  }

  override fun onDisable() {
    changeState(null)
    super.onDisable()
  }

  @SubscribeEvent
  fun onTick(ignored: TickEvent.Start) {
    if (!enabled) {
      return
    }

    if (minecraft.level == null || minecraft.player == null) {
      return
    }

    if (!globalDelay.passed()) {
      return
    }

    state?.onTick()
  }

  @SubscribeEvent
  fun onRender(ignored: WorldEvent.BeforeGizmos) {
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

    state?.onRender()
  }

  fun changeState(newState: ScriptState?) {
    state?.exit()

    newState?.enter()
    state = newState

    ChatUtils.sendSystemMessage("Current State: ${state?.javaClass?.simpleName}", MessageType.DEBUG)
  }

}
