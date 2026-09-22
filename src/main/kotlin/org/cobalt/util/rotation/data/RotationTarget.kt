package org.cobalt.util.rotation.data

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import org.cobalt.util.rotation.RotationMath

sealed interface RotationTarget {

  val targetRotation: Rotation?

  class OfEntity(val entity: Entity) : RotationTarget {
    override val targetRotation: Rotation?
      get() = RotationMath.getRotation(entity.position())
  }

  class OfVector(val vector: Vec3) : RotationTarget {
    override val targetRotation: Rotation?
      get() = RotationMath.getRotation(vector)
  }

  class OfBlockPos(val blockPos: BlockPos) : RotationTarget {
    override val targetRotation: Rotation?
      get() = RotationMath.getRotation(Vec3.atCenterOf(blockPos))
  }

  class Fixed(val rotation: Rotation) : RotationTarget {
    override val targetRotation: Rotation
      get() = rotation
  }

}
