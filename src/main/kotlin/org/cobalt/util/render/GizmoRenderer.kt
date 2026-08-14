package org.cobalt.util.render

import java.awt.Color
import kotlin.math.sin
import net.minecraft.core.BlockPos
import net.minecraft.gizmos.GizmoStyle
import net.minecraft.gizmos.Gizmos
import net.minecraft.util.ARGB
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.cobalt.Cobalt

object GizmoRenderer {

  @JvmStatic
  fun drawBlockPos(
    pos: BlockPos,
    color: Color,
    esp: Boolean = false,
    lineWidth: Float = 1f,
  ) {
    drawBox(
      box = AABB(
        pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
        pos.x + 1.0, pos.y + 1.0, pos.z + 1.0,
      ),
      color = color,
      esp = esp,
      lineWidth = lineWidth,
    )
  }

  @JvmStatic
  fun drawEntityOutline(
    entity: Entity,
    color: Color,
    esp: Boolean = false,
    lineWidth: Float = 1f,
  ) {
    val partialTicks = Cobalt.minecraft.deltaTracker.getGameTimeDeltaPartialTick(true)

    drawBox(
      entity.boundingBox.move(
        entity.xOld + (entity.x - entity.xOld) * partialTicks - entity.x,
        entity.yOld + (entity.y - entity.yOld) * partialTicks - entity.y,
        entity.zOld + (entity.z - entity.zOld) * partialTicks - entity.z,
      ),
      color, esp, lineWidth,
    )
  }

  @JvmStatic
  fun drawTracer(
    to: Vec3,
    color: Color,
    esp: Boolean = true,
    lineWidth: Float = 1f,
  ) {
    if (color.alpha == 0) {
      return
    }

    val camera = Cobalt.minecraft.gameRenderer.mainCamera()
    val from = camera
      .position()
      .add(Vec3.directionFromRotation(camera.xRot(), camera.yRot()))

    drawLine(
      from = from,
      to = to,
      color = color,
      esp = esp,
      lineWidth = lineWidth
    )
  }

  @JvmStatic
  fun drawBox(
    box: AABB,
    color: Color,
    esp: Boolean = false,
    lineWidth: Float = 1f,
  ) {
    if (color.alpha == 0) {
      return
    }

    val props = Gizmos.cuboid(
      box,
      GizmoStyle.strokeAndFill(
        ARGB.color(color.alpha, color.red, color.green, color.blue),
        lineWidth,
        ARGB.color(40, color.red, color.green, color.blue)
      )
    )

    if (esp) {
      props.setAlwaysOnTop()
    }
  }

  @JvmStatic
  fun drawLine(
    from: Vec3,
    to: Vec3,
    color: Color,
    esp: Boolean = false,
    lineWidth: Float = 1f,
  ) {
    if (color.alpha == 0) {
      return
    }

    val props = Gizmos.line(
      from, to,
      ARGB.color(color.alpha, color.red, color.green, color.blue),
      lineWidth
    )

    if (esp) {
      props.setAlwaysOnTop()
    }
  }

  @JvmStatic
  fun drawTargetBeam(
    entity: Entity,
    color: Color,
    esp: Boolean = true,
    lineWidth: Float = 2f,
    speedMs: Double = 1500.0
  ) {
    if (color.alpha == 0) {
      return
    }

    val partialTicks = Cobalt.minecraft.deltaTracker.getGameTimeDeltaPartialTick(true)
    val box = entity.boundingBox.move(
      (entity.xOld - entity.x) * (1 - partialTicks),
      (entity.yOld - entity.y) * (1 - partialTicks),
      (entity.zOld - entity.z) * (1 - partialTicks)
    )

    val centerX = (box.minX + box.maxX) / 2.0
    val centerZ = (box.minZ + box.maxZ) / 2.0
    val radius = (maxOf(box.maxX - box.minX, box.maxZ - box.minZ) / 2.0 + 0.1).toFloat()

    val time = System.currentTimeMillis() % speedMs.toLong()
    val progress1 = (sin((time / speedMs) * 2.0 * Math.PI - Math.PI / 2.0) + 1.0) / 2.0
    val progress2 = 1.0 - progress1

    val totalHeight = box.maxY - box.minY
    val y1 = box.minY + totalHeight * progress1
    val y2 = box.minY + totalHeight * progress2

    val strokeColor = ARGB.color(color.alpha, color.red, color.green, color.blue)
    val fillColor = ARGB.color((color.alpha * 0.35).toInt(), color.red, color.green, color.blue)
    val style = GizmoStyle.strokeAndFill(strokeColor, lineWidth, fillColor)

    val circle1 = Gizmos.circle(Vec3(centerX, y1, centerZ), radius, style)
    val circle2 = Gizmos.circle(Vec3(centerX, y2, centerZ), radius, style)

    if (esp) {
      circle1.setAlwaysOnTop()
      circle2.setAlwaysOnTop()
    }
  }

}
