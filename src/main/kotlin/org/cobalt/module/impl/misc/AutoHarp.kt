package org.cobalt.module.impl.misc

import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.block.Blocks
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.TickEvent
import org.cobalt.module.Module
import org.cobalt.module.ModuleCategory
import org.cobalt.util.input.MouseButton
import org.cobalt.util.inventory.InventoryUtils

object AutoHarp : Module(
  name = "AutoHarp",
  category = ModuleCategory.MISC,
) {

  private var lastInv = 0

  @SubscribeEvent
  fun onTick(ignored: TickEvent.Start) {
    if (!enabled || minecraft.player == null) {
      return
    }

    val menu = minecraft.player?.containerMenu as? ChestMenu ?: return
    val screen = minecraft.screen ?: return

    if (!screen.title.string.startsWith("Harp -") || menu.slots.size < 54) {
      return
    }

    val invHash = menu.slots.subList(0, 36)
      .joinToString("") { slot ->
        slot.item.itemName.string
      }.hashCode()

    if (invHash == lastInv) return
    lastInv = invHash

    repeat(7) {
      val slot = menu.slots[37 + it]

      if ((slot.item.item as? BlockItem)?.block == Blocks.QUARTZ_BLOCK) {
        InventoryUtils.clickSlot(slot.index, MouseButton.MIDDLE, ContainerInput.CLONE)
      }
    }
  }

}
