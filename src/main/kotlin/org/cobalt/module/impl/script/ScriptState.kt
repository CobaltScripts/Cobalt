package org.cobalt.module.impl.script

import net.minecraft.client.Minecraft

open class ScriptState {

  protected val minecraft = Minecraft.getInstance()

  open fun enter() {}
  open fun onTick() {}
  open fun onRender() {}
  open fun exit() {}

}
