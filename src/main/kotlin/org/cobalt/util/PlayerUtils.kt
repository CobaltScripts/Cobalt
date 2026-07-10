package org.cobalt.util

import kotlin.math.ceil
import kotlin.math.floor
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.BlockPos
import org.cobalt.Cobalt.minecraft
import org.cobalt.Cobalt.runOnClientThread
import org.cobalt.util.rotation.Rotation

object PlayerUtils {

  @JvmStatic
  val player: LocalPlayer?
    get() = minecraft.player

  @JvmStatic
  val ign: String
    get() = minecraft.player?.gameProfile?.name ?: "Undefined"

  @get:JvmName("isInventoryEmpty")
  val isInventoryEmpty: Boolean
    get() = minecraft.player?.inventory?.nonEquipmentItems?.all { it.isEmpty } ?: true

  @get:JvmName("isInventoryFull")
  @JvmStatic
  val isInventoryFull: Boolean
    get() = player?.inventory?.nonEquipmentItems?.none { it.isEmpty } ?: false

  @get:JvmName("canFly")
  @JvmStatic
  val canFly: Boolean
    get() = player?.abilities?.mayfly ?: false

  @get:JvmName("isFlying")
  @JvmStatic
  val isFlying: Boolean
    get() = player?.abilities?.flying ?: false

  @get:JvmName("isOnGround")
  @JvmStatic
  val onGround: Boolean
    get() = player?.onGround() ?: false

  @JvmStatic
  val rotation: Rotation
    get() {
      val player = minecraft.player
        ?: return Rotation.ZERO

      return Rotation(player.yRot, player.xRot, true)
    }

  @JvmStatic
  val position: BlockPos
    get() {
      val player = player ?: return BlockPos.ZERO

      return BlockPos(
        floor(player.x).toInt(),
        ceil(player.blockPosition().y.toDouble() - 0.25).toInt(),
        floor(player.z).toInt()
      )
    }

  @JvmStatic
  val isSuffocating: Boolean
    get() {
      val player = minecraft.player ?: return false
      val level = minecraft.level ?: return false

      return !level.noCollision(
        player, player.boundingBox.inflate(-0.15)
      )
    }

  @JvmStatic
  fun setRotation(rotation: Rotation) {
    val player = minecraft.player ?: return

    rotation.normalize().let { rot ->
      player.xRotO = player.xRot
      player.yRotO = player.yRot
      player.yBob = player.yRot
      player.yBobO = player.yRot

      player.yRot = rot.yaw
      player.xRot = rot.pitch
    }
  }

  @JvmStatic
  fun closeScreen() {
    if (minecraft.gui.screen() == null) {
      return
    }

    runOnClientThread {
      player?.closeContainer()
    }
  }

}
