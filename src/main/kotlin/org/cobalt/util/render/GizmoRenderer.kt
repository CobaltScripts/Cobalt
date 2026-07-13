package org.cobalt.util.render

import java.awt.Color
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
      AABB(
        pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
        pos.x + 1.0, pos.y + 1.0, pos.z + 1.0,
      ),
      color, esp, lineWidth,
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
    val camera = Cobalt.minecraft.gameRenderer.mainCamera()
    val from = camera
      .position()
      .add(Vec3.directionFromRotation(camera.xRot(), camera.yRot()))

    drawLine(from, to, color, esp, lineWidth)
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

}
