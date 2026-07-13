package org.cobalt.util.inventory

import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import org.cobalt.Cobalt.minecraft
import org.cobalt.util.input.MouseButton

object InventoryUtils {

  @JvmStatic
  fun clickSlot(
    slot: Int,
    click: MouseButton = MouseButton.LEFT,
    input: ContainerInput = ContainerInput.PICKUP,
  ) {
    val player = minecraft.player ?: return
    val containerId = player.containerMenu.containerId

    minecraft.gameMode?.handleContainerInput(containerId, slot, click.ordinal, input, player)
  }

  @JvmStatic
  fun selectHotbarSlot(slot: Int): Boolean {
    val player = minecraft.player ?: return false

    if (slot !in 0..8) {
      return false
    }

    player.inventory.selectedSlot = slot
    return true
  }

  @JvmStatic
  fun holdItem(name: String): Boolean {
    val slot = findItemInHotbar(name)

    if (slot == -1) {
      return false
    }

    return selectHotbarSlot(slot)
  }

  @JvmStatic
  fun findItemInHotbar(name: String): Int {
    val player = minecraft.player ?: return -1
    val inventory = player.inventory

    return findSlot(9, { inventory.getItem(it) }) {
      it.hoverName.string.contains(name, ignoreCase = true)
    }
  }

  @JvmStatic
  fun findItemInHotbarWithLore(lore: String): Int {
    val player = minecraft.player ?: return -1
    val inventory = player.inventory

    return findSlot(9, { inventory.getItem(it) }) { stack ->
      ItemUtils.getLoreLines(stack).any { it.string.contains(lore, ignoreCase = true) }
    }
  }

  @JvmStatic
  fun findItemInInventory(name: String): Int {
    val player = minecraft.player ?: return -1
    val inventory = player.inventory

    return findSlot(inventory.containerSize, { inventory.getItem(it) }) {
      it.hoverName.string.contains(name, ignoreCase = true)
    }
  }

  @JvmStatic
  fun findItemInInventory(item: Item): Int {
    val player = minecraft.player ?: return -1
    val inventory = player.inventory

    return findSlot(inventory.containerSize, { inventory.getItem(it) }) { it.item == item }
  }

  @JvmStatic
  fun findItemInContainer(name: String): Int {
    val player = minecraft.player ?: return -1
    val menu = player.containerMenu
    val containerSlots = menu.slots.size - player.inventory.nonEquipmentItems.size

    return findSlot(containerSlots, { menu.getSlot(it).item }) {
      it.hoverName.string.contains(name, ignoreCase = true)
    }
  }

  @JvmStatic
  fun findItemInContainer(item: Item): Int {
    val player = minecraft.player ?: return -1
    val menu = player.containerMenu
    val containerSlots = menu.slots.size - player.inventory.nonEquipmentItems.size

    return findSlot(containerSlots, { menu.getSlot(it).item }) { it.item == item }
  }

  @JvmStatic
  fun findItemInInventoryWithLore(lore: String): Int {
    val player = minecraft.player ?: return -1
    val inventory = player.inventory

    return findSlot(inventory.containerSize, { inventory.getItem(it) }) { stack ->
      ItemUtils.getLoreLines(stack).any { it.string.contains(lore, ignoreCase = true) }
    }
  }

  private inline fun findSlot(
    size: Int,
    getStack: (Int) -> ItemStack,
    predicate: (ItemStack) -> Boolean,
  ): Int {
    for (slot in 0 until size) {
      val stack = getStack(slot)

      if (stack.isEmpty) {
        continue
      }

      if (predicate(stack)) {
        return slot
      }
    }

    return -1
  }

}
