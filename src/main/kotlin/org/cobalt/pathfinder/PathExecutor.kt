package org.cobalt.pathfinder

import java.awt.Color
import net.minecraft.client.player.KeyboardInput
import org.cobalt.Cobalt.minecraft
import org.cobalt.dsl.centerVec
import org.cobalt.dsl.smallBox
import org.cobalt.event.EventBus
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.TickEvent
import org.cobalt.event.impl.WorldEvent
import org.cobalt.pathfinder.calculate.Path
import org.cobalt.pathfinder.state.ExecutorState
import org.cobalt.pathfinder.state.impl.CalculatingState
import org.cobalt.ui.theme.ThemeManager
import org.cobalt.util.ChatUtils
import org.cobalt.util.MessageType
import org.cobalt.util.PlayerUtils
import org.cobalt.util.WorldRenderUtils

object PathExecutor {

  var state: ExecutorState? = null
  var config: PathConfig? = null

  var path: Path? = null
  var pathIndex: Int = 0

  var running = false
  var pathInput = PathInput()

  init {
    EventBus.register(this)
  }

  fun goTo(config: PathConfig) {
    stop()

    if (config.useFlyMovement && !PlayerUtils.canFly) {
      ChatUtils.sendSystemMessage("<red>Invalid path config, since player cannot fly!</red>")
      return
    }


    val player = minecraft.player

    if (player != null) {
      player.input = pathInput
    } else {
      return
    }

    this.config = config
    this.running = true

    changeState(CalculatingState())
  }

  fun stop() {
    state?.exit()
    state = null

    minecraft.player?.let {
      it.input = KeyboardInput(minecraft.options)
    }.also {
      pathInput.stopMovement()
    }

    running = false
    config = null

    path = null
    pathIndex = 0
  }

  fun changeState(newState: ExecutorState) {
    state?.exit()
    state = newState
    state?.enter()

    ChatUtils.sendSystemMessage(
      "Entering ${newState.javaClass.simpleName} Executor State",
      MessageType.DEBUG
    )
  }

  @SubscribeEvent
  fun onTick(ignored: TickEvent.Start) {
    if (minecraft.level == null || minecraft.player == null) {
      stop()
      return
    }

    if (minecraft.gui.screen() != null) {
      pathInput.stopMovement()
      return
    }

    if (!running) {
      return
    }

    state?.onTick()
  }

  @SubscribeEvent
  fun onRender(ignored: WorldEvent.Render) {
    if (!running) {
      return
    }

    state?.onRender()

    if (state is CalculatingState) {
      return
    }

    val theme = ThemeManager.activeTheme
    val nodes = path?.keyNodes ?: return

    val targetNode = nodes[pathIndex].centerVec
    val playerPos = PlayerUtils.position.centerVec()

    WorldRenderUtils.drawBox(playerPos.smallBox(), Color.GREEN)
    WorldRenderUtils.drawBox(targetNode.smallBox(), Color.RED)

    for (index in nodes.indices) {
      val node = nodes[index]

      WorldRenderUtils.drawBlockPos(
        if (node.isFly) node.block else node.blockStandingOn,
        color = theme.accentPrimary
      )

      if (index > 0) {
        val prev = nodes[index - 1]

        WorldRenderUtils.drawLine(
          prev.topCenterVec, node.topCenterVec,
          theme.accentSecondary
        )
      }
    }
  }

}
