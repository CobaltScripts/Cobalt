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
  @JvmOverloads
  fun holdItem(name: String, exactMatch: Boolean = false): Boolean {
    val slot = findItemInHotbar(name, exactMatch)

    if (slot == -1) {
      return false
    }

    return selectHotbarSlot(slot)
  }

  @JvmStatic
  @JvmOverloads
  fun findItemInHotbar(name: String, exactMatch: Boolean = false): Int {
    val player = minecraft.player ?: return -1
    val inventory = player.inventory

    return findSlot(9, { inventory.getItem(it) }) { _, stack ->
      matches(stack.displayName.string, name, exactMatch)
    }
  }

  @JvmStatic
  @JvmOverloads
  fun findItemInHotbarWithLore(lore: String, exactMatch: Boolean = false): Int {
    val player = minecraft.player ?: return -1
    val inventory = player.inventory

    return findSlot(9, { inventory.getItem(it) }) { _, stack ->
      ItemUtils.getLoreLines(stack).any {
        matches(it.string, lore, exactMatch)
      }
    }
  }

  @JvmStatic
  @JvmOverloads
  fun findItemInInventory(name: String, exactMatch: Boolean = false): Int {
    val player = minecraft.player ?: return -1
    val menu = player.containerMenu

    return findSlot(menu.slots.size, { menu.getSlot(it).item }) { slot, stack ->
      menu.getSlot(slot).container == player.inventory &&
        matches(stack.displayName.string, name, exactMatch)
    }
  }

  @JvmStatic
  fun findItemInInventory(item: Item): Int {
    val player = minecraft.player ?: return -1
    val menu = player.containerMenu

    return findSlot(menu.slots.size, { menu.getSlot(it).item }) { slot, stack ->
      menu.getSlot(slot).container == player.inventory &&
        stack.item == item
    }
  }

  @JvmStatic
  @JvmOverloads
  fun findItemInContainer(name: String, exactMatch: Boolean = false): Int {
    val player = minecraft.player ?: return -1
    val menu = player.containerMenu

    return findSlot(menu.slots.size, { menu.getSlot(it).item }) { slot, stack ->
      menu.getSlot(slot).container != player.inventory &&
        matches(stack.displayName.string, name, exactMatch)
    }
  }

  @JvmStatic
  fun findItemInContainer(item: Item): Int {
    val player = minecraft.player ?: return -1
    val menu = player.containerMenu

    return findSlot(menu.slots.size, { menu.getSlot(it).item }) { slot, stack ->
      menu.getSlot(slot).container != player.inventory &&
        stack.item == item
    }
  }

  @JvmStatic
  @JvmOverloads
  fun findItemInInventoryWithLore(lore: String, exactMatch: Boolean = false): Int {
    val player = minecraft.player ?: return -1
    val menu = player.containerMenu

    return findSlot(menu.slots.size, { menu.getSlot(it).item }) { slot, stack ->
      menu.getSlot(slot).container == player.inventory &&
        ItemUtils.getLoreLines(stack).any {
          matches(it.string, lore, exactMatch)
        }
    }
  }

  private fun matches(text: String, query: String, exactMatch: Boolean): Boolean {
    return if (exactMatch) {
      text.equals(query, ignoreCase = true)
    } else {
      text.contains(query, ignoreCase = true)
    }
  }

  private fun findSlot(
    size: Int,
    getStack: (Int) -> ItemStack,
    predicate: (slot: Int, stack: ItemStack) -> Boolean,
  ): Int {
    for (slot in 0 until size) {
      val stack = getStack(slot)

      if (stack.isEmpty) {
        continue
      }

      if (predicate(slot, stack)) {
        return slot
      }
    }

    return -1
  }

}
