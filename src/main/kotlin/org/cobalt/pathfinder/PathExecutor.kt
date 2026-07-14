package org.cobalt.pathfinder

import net.minecraft.client.player.KeyboardInput
import org.cobalt.Cobalt.minecraft
import org.cobalt.event.EventBus
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.TickEvent
import org.cobalt.event.impl.WorldEvent
import org.cobalt.pathfinder.calculate.Path
import org.cobalt.pathfinder.helper.MovementController
import org.cobalt.pathfinder.state.ExecutorState
import org.cobalt.pathfinder.state.impl.CalculatingState
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.chat.MessageType
import org.cobalt.util.client.PlayerUtils

object PathExecutor {

  var state: ExecutorState? = null
  var config: PathConfig? = null

  var path: Path? = null
  var pathIndex: Int = 1

  var running = false
  var movementController = MovementController()

  init {
    EventBus.register(this)
  }

  fun goTo(config: PathConfig) {
    val player = minecraft.player ?: run {
      ChatUtils.sendSystemMessage(
        "Tried running pathfinder, but minecraft.player is null!",
        MessageType.DEBUG
      )

      return
    }

    if (running) {
      ChatUtils.sendSystemMessage(
        "Stopping current pathfinder because a new one started.",
        MessageType.DEBUG
      )

      stop()
    }

    if (config.allowFly && !PlayerUtils.canFly) {
      ChatUtils.sendSystemMessage("<red>Invalid path config, since player cannot fly!</red>")
      return
    }

//    player.input = movementController

    this.config = config
    this.running = true

    changeState(CalculatingState())
  }

  fun stop() {
    state?.exit()
    state = null

//    minecraft.player?.let {
//      it.input = KeyboardInput(minecraft.options)
//    }.also {
//      movementController.stopMovement()
//    }

    running = false

    path = null
    pathIndex = 1
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
      movementController.stopMovement()
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

    if (state !is CalculatingState) {
      PathRenderer.render()
    }

    state?.onRender()
  }

}
