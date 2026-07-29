package org.cobalt.module.impl.misc

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.random.Random
import org.cobalt.event.EventBus
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.WorldEvent
import org.cobalt.module.Module
import org.cobalt.module.ModuleCategory
import org.cobalt.ui.component.setting.impl.SliderSetting
import org.cobalt.util.client.PlayerUtils
import org.cobalt.util.input.Mouse
import org.cobalt.util.input.MouseMode
import org.cobalt.util.rotation.RotationMath
import org.cobalt.util.rotation.data.Rotation
import org.cobalt.util.rotation.data.RotationTarget

object Rotations : Module(
  name = "Rotations",
  category = ModuleCategory.MISC,
  toggleable = false,
  startValue = true
) {

  var running = false
    private set

  private var mode: RotationType = RotationType.ROTATE
  private var target: RotationTarget? = null
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

  val trackingSpeed by SliderSetting(
    name = "Tracking Speed",
    description = "How fast rotations track the target (higher = faster)",
    min = 1,
    max = 50,
    defaultValue = 25
  )

  init {
    EventBus.register(this)
  }

  fun start(rotationTarget: Rotation) {
    mode = RotationType.ROTATE
    target = RotationTarget(rotationTarget)
    lastFrameMs = System.currentTimeMillis()
    running = true

    if (Mouse.mouseMode == MouseMode.DEFAULT) {
      Mouse.mouseMode = MouseMode.LOCK_MOUSE
      returnMouseMode = true
    }
  }

  fun track(rotationTarget: RotationTarget) {
    mode = RotationType.TRACK
    target = rotationTarget
    running = true

    if (Mouse.mouseMode == MouseMode.DEFAULT) {
      Mouse.mouseMode = MouseMode.LOCK_MOUSE
      returnMouseMode = true
    }
  }

  fun stop() {
    running = false
    target = null

    if (returnMouseMode) {
      Mouse.mouseMode = MouseMode.DEFAULT
      returnMouseMode = false
    }
  }

  @SubscribeEvent
  fun onRender(ignored: WorldEvent.BeforeGizmos) {
    if (!running || minecraft.screen != null) {
      return
    }

    val targetObj = target ?: return
    val currentTargetRotation = targetObj.targetRotation

    val now = System.currentTimeMillis()
    val deltaTime = ((now - lastFrameMs) / 50f).coerceIn(0f, 1f)
    val current = PlayerUtils.rotation

    lastFrameMs = now

    when (mode) {
      RotationType.ROTATE -> handleRotate(current, currentTargetRotation, deltaTime)
      RotationType.TRACK -> handleTrack(current, currentTargetRotation, deltaTime)
    }
  }

  private fun handleRotate(current: Rotation, target: Rotation, deltaTime: Float) {
    if (RotationMath.approximatelyEquals(current, target, endTolerance.toFloat())) {
      stop()
      return
    }

    val delta = current.rotationDeltaTo(target)
    val stepYaw = smoothStep(abs(delta.deltaYaw), turnSpeedYaw.toFloat(), deltaTime)
    val stepPitch = smoothStep(abs(delta.deltaPitch), turnSpeedPitch.toFloat(), deltaTime)

    applyRotation(
      current,
      stepYaw * if (delta.deltaYaw >= 0f) 1f else -1f,
      stepPitch * if (delta.deltaPitch >= 0f) 1f else -1f
    )
  }

  private fun handleTrack(current: Rotation, target: Rotation, deltaTime: Float) {
    if (RotationMath.approximatelyEquals(current, target, endTolerance.toFloat())) {
      return
    }

    var needYaw = RotationMath.angleDifference(target.yaw, current.yaw)
    var needPitch = RotationMath.angleDifference(target.pitch, current.pitch)
    val distance = abs(needYaw) + abs(needPitch)

    val randomFactor = (0.8f + Random.nextFloat() * 0.4f)
    val deceleration = 1f / maxOf(distance / 80f, 1f)

    needYaw *= (trackingSpeed / 100f) * randomFactor * deltaTime * deceleration
    needPitch *= (trackingSpeed / 100f) * randomFactor * deltaTime * deceleration

    val newYaw = current.yaw + needYaw
    val newPitch = (current.pitch + needPitch).coerceIn(-75f, 75f)

    PlayerUtils.setRotation(Rotation(newYaw, newPitch).normalize(current))
  }

  private fun applyRotation(current: Rotation, stepYaw: Float, stepPitch: Float) {
    val next = Rotation(
      yaw = current.yaw + stepYaw,
      pitch = current.pitch + stepPitch
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

  enum class RotationType {
    ROTATE,
    TRACK
  }

}
