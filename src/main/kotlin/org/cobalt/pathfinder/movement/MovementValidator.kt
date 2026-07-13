package org.cobalt.pathfinder.movement

import org.cobalt.pathfinder.movement.rules.PassthroughRules.canPassThrough
import org.cobalt.pathfinder.movement.rules.StandingRules.canStandOn

object MovementValidator {

  @JvmStatic
  fun canWalkOn(calculationContext: CalculationContext, x: Int, y: Int, z: Int): Boolean {
    if (!canWalkThrough(calculationContext, x, y + 1, z)) {
      return false
    }

    return canStandOn(calculationContext.blockStateAccessor, x, y, z)
  }

  @JvmStatic
  fun canWalkThrough(calculationContext: CalculationContext, x: Int, y: Int, z: Int): Boolean {
    return canPassThrough(calculationContext.blockStateAccessor, x, y, z) &&
      canPassThrough(calculationContext.blockStateAccessor, x, y + 1, z)
  }

}
