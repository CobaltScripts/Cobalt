package org.cobalt

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
import org.cobalt.internal.loader.AddonLoader

class PreLaunch : PreLaunchEntrypoint {

  override fun onPreLaunch() {
    AddonLoader.findAddons()
  }

}
