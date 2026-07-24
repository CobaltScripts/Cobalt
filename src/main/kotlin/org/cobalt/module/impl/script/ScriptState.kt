package org.cobalt.module.script

import net.minecraft.client.Minecraft

abstract class ScriptState {

  protected val minecraft = Minecraft.getInstance()

  open fun enter() {}
  open fun onTick() {}
  open fun onRender() {}
  open fun exit() {}

}
