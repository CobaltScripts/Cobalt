package org.cobalt.pathfinder.movement

import net.minecraft.util.Mth
import org.cobalt.pathfinder.helper.PlayerInput

object MovementInputMapper {
  private const val DIRECTIONS_COUNT = 8
  private const val FULL_CIRCLE_DEGREES = 360f
  private const val SECTOR_WIDTH_DEGREES = FULL_CIRCLE_DEGREES / DIRECTIONS_COUNT
  private const val SECTOR_HALF_WIDTH_DEGREES = SECTOR_WIDTH_DEGREES / 2f

  private const val DEAD_BAND_DEGREES = 0.05f

  private const val FORWARD_RIGHT_START_DEGREES = SECTOR_HALF_WIDTH_DEGREES
  private const val RIGHT_START_DEGREES = FORWARD_RIGHT_START_DEGREES + SECTOR_WIDTH_DEGREES
  private const val BACK_RIGHT_START_DEGREES = RIGHT_START_DEGREES + SECTOR_WIDTH_DEGREES
  private const val BACKWARD_START_DEGREES = BACK_RIGHT_START_DEGREES + SECTOR_WIDTH_DEGREES

  private const val FORWARD_LEFT_START_DEGREES = -FORWARD_RIGHT_START_DEGREES
  private const val LEFT_START_DEGREES = -RIGHT_START_DEGREES
  private const val BACK_LEFT_START_DEGREES = -BACK_RIGHT_START_DEGREES
  private const val BACKWARD_NEG_START_DEGREES = -BACKWARD_START_DEGREES

  private const val FORWARD_RIGHT_HOLD_DEGREES = FORWARD_RIGHT_START_DEGREES + DEAD_BAND_DEGREES
  private const val RIGHT_HOLD_DEGREES = RIGHT_START_DEGREES + DEAD_BAND_DEGREES
  private const val BACK_RIGHT_HOLD_DEGREES = BACK_RIGHT_START_DEGREES + DEAD_BAND_DEGREES
  private const val BACKWARD_HOLD_DEGREES = BACKWARD_START_DEGREES + DEAD_BAND_DEGREES

  private const val FORWARD_LEFT_HOLD_DEGREES = FORWARD_LEFT_START_DEGREES - DEAD_BAND_DEGREES
  private const val LEFT_HOLD_DEGREES = LEFT_START_DEGREES - DEAD_BAND_DEGREES
  private const val BACK_LEFT_HOLD_DEGREES = BACK_LEFT_START_DEGREES - DEAD_BAND_DEGREES
  private const val BACKWARD_NEG_HOLD_DEGREES = BACKWARD_NEG_START_DEGREES - DEAD_BAND_DEGREES

  private enum class Sector { FORWARD, FORWARD_RIGHT, RIGHT, BACK_RIGHT, BACKWARD, BACK_LEFT, LEFT, FORWARD_LEFT }

  private var lastSector: Sector = Sector.FORWARD

  @JvmStatic
  fun getNeededKeys(playerYaw: Float, idealYaw: Float): PlayerInput {
    val diff = Mth.wrapDegrees(idealYaw - playerYaw)

    if (staysInSector(lastSector, diff)) {
      return sectorToInput(lastSector)
    }

    val newSector = classify(diff)
    lastSector = newSector
    return sectorToInput(newSector)
  }

  private fun staysInSector(sector: Sector, diff: Float): Boolean = when (sector) {
    Sector.FORWARD -> diff in FORWARD_LEFT_HOLD_DEGREES..<FORWARD_RIGHT_HOLD_DEGREES
    Sector.FORWARD_RIGHT -> diff >= FORWARD_RIGHT_START_DEGREES - DEAD_BAND_DEGREES && diff < RIGHT_HOLD_DEGREES
    Sector.RIGHT -> diff >= RIGHT_START_DEGREES - DEAD_BAND_DEGREES && diff < BACK_RIGHT_HOLD_DEGREES
    Sector.BACK_RIGHT -> diff >= BACK_RIGHT_START_DEGREES - DEAD_BAND_DEGREES && diff < BACKWARD_HOLD_DEGREES
    Sector.BACKWARD -> diff >= BACKWARD_START_DEGREES - DEAD_BAND_DEGREES || diff < BACKWARD_NEG_HOLD_DEGREES
    Sector.BACK_LEFT -> diff >= BACKWARD_NEG_START_DEGREES - DEAD_BAND_DEGREES && diff < BACK_LEFT_HOLD_DEGREES
    Sector.LEFT -> diff >= BACK_LEFT_START_DEGREES - DEAD_BAND_DEGREES && diff < LEFT_HOLD_DEGREES
    Sector.FORWARD_LEFT -> diff >= LEFT_START_DEGREES - DEAD_BAND_DEGREES && diff < FORWARD_LEFT_HOLD_DEGREES
  }

  private fun classify(diff: Float): Sector = when (diff) {
      in FORWARD_LEFT_START_DEGREES..<FORWARD_RIGHT_START_DEGREES -> Sector.FORWARD
      in FORWARD_RIGHT_START_DEGREES..<RIGHT_START_DEGREES -> Sector.FORWARD_RIGHT
      in RIGHT_START_DEGREES..<BACK_RIGHT_START_DEGREES -> Sector.RIGHT
      in BACK_RIGHT_START_DEGREES..<BACKWARD_START_DEGREES -> Sector.BACK_RIGHT
      !in BACKWARD_NEG_START_DEGREES..<BACKWARD_START_DEGREES -> Sector.BACKWARD
      in BACKWARD_NEG_START_DEGREES..<BACK_LEFT_START_DEGREES -> Sector.BACK_LEFT
      in BACK_LEFT_START_DEGREES..<LEFT_START_DEGREES -> Sector.LEFT
      else -> Sector.FORWARD_LEFT
  }

  private fun sectorToInput(sector: Sector): PlayerInput = when (sector) {
    Sector.FORWARD -> PlayerInput(forward = true)
    Sector.FORWARD_RIGHT -> PlayerInput(forward = true, right = true)
    Sector.RIGHT -> PlayerInput(right = true)
    Sector.BACK_RIGHT -> PlayerInput(backward = true, right = true)
    Sector.BACKWARD -> PlayerInput(backward = true)
    Sector.BACK_LEFT -> PlayerInput(backward = true, left = true)
    Sector.LEFT -> PlayerInput(left = true)
    Sector.FORWARD_LEFT -> PlayerInput(forward = true, left = true)
  }
}
