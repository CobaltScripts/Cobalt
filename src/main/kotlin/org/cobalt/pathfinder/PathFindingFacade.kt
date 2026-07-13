package org.cobalt.pathfinder

import net.minecraft.client.player.KeyboardInput
import org.cobalt.Cobalt.minecraft
import org.cobalt.event.EventBus
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.TickEvent
import org.cobalt.event.impl.WorldEvent
import org.cobalt.pathfinder.calculate.Path
import org.cobalt.pathfinder.calculate.PathMode
import org.cobalt.pathfinder.goal.Goal
import org.cobalt.pathfinder.movement.Movement
import org.cobalt.pathfinder.state.ExecutorState
import org.cobalt.pathfinder.state.impl.CalculatingState
import org.cobalt.util.ChatUtils
import org.cobalt.util.MessageType
import org.cobalt.util.PlayerUtils

object PathFindingFacade {
  var state: ExecutorState? = null

  var currentGoal: Goal? = null
  var availableMovements: Array<out Movement>? = null

  var path: Path? = null
  var pathIndex: Int = 0

  var running = false
  var pathInput = PathInput()

  init {
    EventBus.register(this)
  }

  fun goTo(goal: Goal, fly: Boolean = false) {
    // Are we sure we want to do this? There's no warning or anything.
    // I've kept it but with a debug message
    if (running) {
      ChatUtils.sendSystemMessage(
        "Stopping current pathfinder because a new one started.",
        MessageType.DEBUG
      )
      stop()
    }

    if (fly && !PlayerUtils.canFly) {
      ChatUtils.sendSystemMessage("<red>Invalid path config, since player cannot fly!</red>")
      return
    }

    val player = minecraft.player

    if (player == null) {
      ChatUtils.sendSystemMessage(
        "Tried running pathfinder, but minecraft.player is null!",
        MessageType.DEBUG
      )
      return
    } else {
      player.input = pathInput
    }

    availableMovements = if (fly) PathMode.FLY.movements else PathMode.WALK.movements
    currentGoal = goal

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

//    PathRenderer.render()
  }

}
