package org.cobalt.module.type

import org.cobalt.module.Module
import org.cobalt.module.ModuleCategory

abstract class Failsafe @JvmOverloads constructor(
  name: String,
  val priority: Int,
  startValue: Boolean = true
) : Module(
  name = name,
  category = ModuleCategory.FAILSAFE,
  startValue = startValue
) {

  abstract fun resetStates()
  abstract fun performReaction(): ReactionResult?

  enum class ReactionResult {
    CONTINUE,
    FINISHED
  }

}
