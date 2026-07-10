package org.cobalt.dsl

import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

fun BlockPos.centerVec(): Vec3 {
  return Vec3(x + 0.5, y + 0.5, z + 0.5)
}

fun Vec3.smallBox(): AABB {
  return AABB(
    x - 0.25,
    y - 0.25,
    z - 0.25,
    x + 0.25,
    y + 0.25,
    z + 0.25
  )
}
