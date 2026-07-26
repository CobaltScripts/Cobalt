package org.cobalt.module.type

import org.cobalt.event.EventBus
import org.cobalt.module.Module
import org.cobalt.module.ModuleCategory
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.input.Mouse
import org.cobalt.util.input.MouseMode

open class Script @JvmOverloads constructor(
  name: String,
  category: ModuleCategory,
  val backgroundResourcePath: String = "",
  val failsafes: List<Failsafe> = emptyList(),
) : Module(
  name, category,
  toggleable = false,
  startValue = false
) {

  override val identifier: String = name.replace(" ", "")
  override val directoryPath: String = "scripts"

  var paused: Boolean = false
    private set

  internal fun startScript() {
    if (enabled) {
      return
    }

    Mouse.mouseMode = MouseMode.UNGRAB_MOUSE
    ChatUtils.sendSystemMessage("$name Script has been <green>Enabled</green>")
    enabled = true
  }

  internal fun stopScript() {
    if (!enabled) {
      return
    }

    Mouse.mouseMode = MouseMode.DEFAULT
    ChatUtils.sendSystemMessage("$name Script has been <red>Disabled</red>")
    enabled = false
  }

  internal fun pause() {
    if (paused) return

    paused = true
    onPause()
    EventBus.unregister(this)
  }

  internal fun resume() {
    if (!paused) return

    paused = false
    EventBus.register(this)
    onResume()
  }

  open fun failsafeDelayTicks(): Int {
    return 5
  }

  open fun onPause() {}
  open fun onResume() {}

}
