package org.cobalt.internal.modules

import net.minecraft.client.MinecraftClient
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import org.cobalt.api.util.ChatUtils

// TODO: Move to QOL Addon
object NameProtect {

  var isEnabled = false
  var newName = "Cobalt User"
  var isGradient = true
  var startGradient = 0xFF4A90E2.toInt()
  var endGradient = 0xFF2C5DA3.toInt()

  fun setName(name: String) {
    newName = name
  }

  fun setGradient(state: Boolean, sg: Int = startGradient, eg: Int = endGradient) {
    isGradient = state
    startGradient = sg
    endGradient = eg
  }

  @JvmStatic
  fun getName(): MutableText {
    return if (isGradient) {
        ChatUtils.buildGradient(newName, startGradient, endGradient)
    } else {
      Text.literal(newName)
    }
  }

  @JvmStatic
  fun getMcIGN(): String {
    val name = MinecraftClient.getInstance().player?.gameProfile?.name
    return name.toString()
  }

}
