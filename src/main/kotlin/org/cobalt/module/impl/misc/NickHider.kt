package org.cobalt.module.impl.misc

import org.cobalt.module.Module
import org.cobalt.module.ModuleCategory
import org.cobalt.ui.component.setting.impl.TextSetting

object NickHider : Module(
  name = "NickHider",
  category = ModuleCategory.MISC,
) {

  val nickname by TextSetting(
    name = "Nickname",
    description = "Your new nickname",
    defaultValue = "CobaltUser"
  )

}
