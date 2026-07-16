package org.cobalt.pathfinder.movement.walk

import org.cobalt.pathfinder.PathConfig
import org.cobalt.pathfinder.calculate.PathNode
import org.cobalt.pathfinder.movement.CalculationContext
import org.cobalt.pathfinder.movement.Movement
import org.cobalt.pathfinder.movement.MovementResult
import org.cobalt.pathfinder.movement.MovementState
import org.cobalt.pathfinder.movement.MovementType
import org.cobalt.pathfinder.movement.MovementValidator
import org.cobalt.util.block.BlockUtils
import kotlin.math.max
import kotlin.math.min
import kotlin.math.ceil
import org.cobalt.pathfinder.movement.MovementStatus
import org.cobalt.pathfinder.movement.MovementTarget
import org.cobalt.util.client.PlayerUtils

class MovementAscend(
  val dx: Int,
  val dz: Int,
) : Movement(MovementType.WALK) {

  override fun updateState(config: PathConfig, nodes: List<PathNode>, currNodeIndex: Int): MovementState {
    val targetNode = nodes[currNodeIndex]

    if (PlayerUtils.blockStandingOn == targetNode.block) {
      return MovementState(status = MovementStatus.REACHED)
    }

    return MovementState(MovementTarget())
  }

  override fun calculateCost(
    ctx: CalculationContext,
    currNode: PathNode,
    res: MovementResult,
  ) {
    val x = currNode.x + dx
    val z = currNode.z + dz

    val maxJumpHeight = 1.125 + max(0, ctx.jumpAmplifier) * 0.5
    val maxJumpBlockCeil = ceil(maxJumpHeight).toInt().coerceAtLeast(1)
    val maxJumpBlocks = min(ctx.costs.blockFallCost.lastIndex, maxJumpBlockCeil)
    var y = currNode.y + 1
    var landingY: Int? = null

    while (y <= currNode.y + maxJumpBlocks) {
      if (!MovementValidator.canWalkThrough(ctx, currNode.x, y + 1, currNode.z)) {
        break
      }

      if (
        MovementValidator.canWalkOn(ctx, x, y, z) &&
        MovementValidator.canWalkThrough(ctx, x, y + 1, z)
      ) {
        landingY = y
        break
      }

      y++
    }

    if (landingY == null) {
      return
    }

    val sourceHeight = BlockUtils.getCollisionHeight(ctx.bsa, currNode.x, currNode.y, currNode.z)
    val destHeight = BlockUtils.getCollisionHeight(ctx.bsa, x, landingY, z)
    val diff = (landingY - currNode.y).toDouble() + destHeight - sourceHeight
    val jumpProgress = (diff / maxJumpHeight).coerceIn(0.0, 1.0)
    val jumpPreference = (1.0 - jumpProgress) * ctx.costs.oneBlockWalkCost * 0.75

    res.set(x, landingY, z)

    val extraWalkBlocks = (landingY - currNode.y - 1).coerceAtLeast(0)

    res.cost = when {
      diff <= 0.5 -> ctx.costs.oneBlockWalkCost
      diff <= maxJumpHeight -> ctx.costs.jumpOneBlockCost + extraWalkBlocks * ctx.costs.oneBlockWalkCost - jumpPreference
      else -> ctx.costs.infCost
    }
  }

  companion object {
    val NORTH = MovementAscend(0, -1)
    val SOUTH = MovementAscend(0, 1)
    val EAST = MovementAscend(1, 0)
    val WEST = MovementAscend(-1, 0)
  }

}
