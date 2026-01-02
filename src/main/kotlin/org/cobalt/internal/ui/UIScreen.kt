package org.cobalt.internal.ui

import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.cobalt.Cobalt
import org.cobalt.api.util.TickScheduler

internal abstract class UIScreen : Screen(Text.empty()) {

  fun openUI() =
    TickScheduler.schedule(1) { Cobalt.mc.setScreen(this) }

  override fun shouldPause() = false

}
