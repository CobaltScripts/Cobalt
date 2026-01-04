package org.cobalt.api.util

import net.minecraft.client.MinecraftClient
import net.minecraft.util.Hand

object Keybinds {

    private val mc: MinecraftClient
        get() = MinecraftClient.getInstance()

    fun rightClick() {
        val client = mc
        val player = client.player ?: return
        val interactionManager = client.interactionManager ?: return

        interactionManager.interactItem(player, Hand.MAIN_HAND)
    }
}
