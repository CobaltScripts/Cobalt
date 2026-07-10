package org.cobalt.util.rotation

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import org.cobalt.util.RotationUtils

class RotationTarget {

  private var entity: Entity? = null
  private var vector: Vec3? = null
  private var blockPos: BlockPos? = null
  private var rotation: Rotation? = null

  val targetRotation: Rotation
    get() {
      rotation?.let {
        return it
      }
      val vec = vector
        ?: entity?.position()
        ?: blockPos?.let { Vec3.atCenterOf(it) }
        ?: error("No rotation target set.")

      return RotationUtils.getRotation(vec)
    }

  constructor(entityTarget: Entity) {
    entity = entityTarget
  }

  constructor(vectorTarget: Vec3) {
    vector = vectorTarget
  }

  constructor(blockPosTarget: BlockPos) {
    blockPos = blockPosTarget
  }

  constructor(rotationTarget: Rotation) {
    rotation = rotationTarget
  }

}
