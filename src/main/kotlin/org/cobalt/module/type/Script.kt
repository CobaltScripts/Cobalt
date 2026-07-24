package org.cobalt.module.type

import org.cobalt.module.Module
import org.cobalt.module.ModuleCategory
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.input.Mouse
import org.cobalt.util.input.MouseMode

open class Script(
  name: String,
  category: ModuleCategory,
  val backgroundResourcePath: String = "",
) : Module(
  name, category,
  toggleable = false,
  startValue = false
) {

  override val identifier: String = name.replace(" ", "")
  override val directoryPath: String = "scripts"

  fun startScript() {
    if (enabled) {
      return
    }

    Mouse.mouseMode = MouseMode.UNGRAB_MOUSE
    ChatUtils.sendSystemMessage("$name Script has been <green>Enabled</green>")
    enabled = true
  }

  fun stopScript() {
    if (!enabled) {
      return
    }

    Mouse.mouseMode = MouseMode.DEFAULT
    ChatUtils.sendSystemMessage("$name Script has been <red>Disabled</red>")
    enabled = false
  }

}
