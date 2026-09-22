package org.cobalt.module.type

import net.fabricmc.loader.api.FabricLoader
import org.cobalt.module.Module
import org.cobalt.module.ModuleCategory
import org.cobalt.module.ModuleManager

abstract class Failsafe @JvmOverloads constructor(
  name: String,
  val priority: Int,
  startValue: Boolean = true
) : Module(
  name = name,
  category = ModuleCategory.FAILSAFE,
  startValue = startValue
) {

  protected fun shouldReactToEvents(): Boolean =
    ModuleManager.isScriptRunning() || FabricLoader.getInstance().isDevelopmentEnvironment

  abstract fun resetStates()
  abstract fun performReaction(): ReactionResult?

  enum class ReactionResult {
    CONTINUE,
    FINISHED
  }

}
