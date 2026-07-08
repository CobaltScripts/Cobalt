package org.cobalt.module.impl.misc

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import org.cobalt.event.EventBus
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.WorldEvent
import org.cobalt.module.Module
import org.cobalt.module.ModuleCategory
import org.cobalt.ui.component.setting.impl.SliderSetting
import org.cobalt.util.MouseMode
import org.cobalt.util.MouseUtils
import org.cobalt.util.PlayerUtils
import org.cobalt.util.RotationUtils
import org.cobalt.util.rotation.Rotation

object Rotations : Module(
  name = "Rotations",
  category = ModuleCategory.MISC,
  toggleable = false,
  startValue = true
) {

  var running = false
    private set

  private var targetRotation: Rotation? = null
  private var lastFrameMs = 0L
  private var returnMouseMode = false

  val turnSpeedYaw by SliderSetting(
    name = "Turn Speed Yaw",
    description = "Yaw turn speed in degrees/tick",
    min = 1,
    max = 180,
    defaultValue = 45
  )

  val turnSpeedPitch by SliderSetting(
    name = "Turn Speed Pitch",
    description = "Pitch turn speed in degrees/tick",
    min = 1,
    max = 90,
    defaultValue = 30
  )

  val midpoint by SliderSetting(
    name = "Midpoint",
    description = "Bezier/sigmoid crossover point",
    min = 0,
    max = 100,
    defaultValue = 35
  )

  val smoothing by SliderSetting(
    name = "Smoothing",
    description = "How much to ease",
    min = 1,
    max = 10,
    defaultValue = 5
  )
  val endTolerance by SliderSetting(
    name = "End Tolerance",
    description = "Rotation completion tolerance in degrees",
    min = 1,
    max = 5,
    defaultValue = 1
  )

  init {
    EventBus.register(this)
  }

  fun start(target: Rotation) {
    targetRotation = target
    lastFrameMs = System.currentTimeMillis()
    running = true

    if (MouseUtils.mouseMode == MouseMode.DEFAULT) {
      MouseUtils.mouseMode = MouseMode.LOCK_MOUSE
      returnMouseMode = true
    }
  }

  fun stop() {
    running = false
    targetRotation = null

    if (returnMouseMode) {
      MouseUtils.mouseMode = MouseMode.DEFAULT
      returnMouseMode = false
    }
  }

  @SubscribeEvent
  fun onRender(ignored: WorldEvent.Render) {
    if (!running || minecraft.gui.screen() != null) {
      return
    }

    val target = targetRotation ?: return

    val now = System.currentTimeMillis()
    val deltaTime = ((now - lastFrameMs) / 50f).coerceIn(0f, 1f)
    val current = PlayerUtils.rotation

    lastFrameMs = now

    if (RotationUtils.approximatelyEquals(current, target, endTolerance.toFloat())) {
      stop()
      return
    }

    val delta = current.rotationDeltaTo(target)
    val stepYaw = smoothStep(abs(delta.deltaYaw), turnSpeedYaw.toFloat(), deltaTime)
    val stepPitch = smoothStep(abs(delta.deltaPitch), turnSpeedPitch.toFloat(), deltaTime)

    val next = Rotation(
      yaw = current.yaw + stepYaw * if (delta.deltaYaw >= 0f) 1f else -1f,
      pitch = current.pitch + stepPitch * if (delta.deltaPitch >= 0f) 1f else -1f
    )

    PlayerUtils.setRotation(next.normalize(current))
  }

  private fun smoothStep(remainingDeg: Float, turnSpeed: Float, deltaTime: Float): Float {
    val t = (remainingDeg / 180f).coerceIn(0f, 1f)

    val factor = if (t > midpoint / 100) {
      bezier(0.05f, 1f, 1f - t)
    } else {
      sigmoid(t)
    }

    val power = 0.5f + smoothing.toFloat() * 0.25f
    val smoothed = factor.toDouble().pow(power.toDouble()).toFloat()

    return (smoothed * remainingDeg * deltaTime)
      .coerceIn(0f, turnSpeed * deltaTime)
  }

  private fun sigmoid(t: Float): Float =
    1f / (1f + exp(-0.5f * (t - 0.3f)))

  private fun bezier(start: Float, end: Float, t: Float): Float =
    (1f - t) * (1f - t) * start + 2f * (1f - t) * t * 1f + t * t * end

}
