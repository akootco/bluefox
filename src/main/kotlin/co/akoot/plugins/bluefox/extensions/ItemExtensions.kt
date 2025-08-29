package co.akoot.plugins.bluefox.extensions

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

val Material.itemStack get() = ItemStack(this)
fun Material.itemStack(amount: Int): ItemStack = ItemStack(this, amount)

/**
 * Gives items to the player's inventory in block form and remainder items based on a specified ratio.
 * Converts the given amount of an item to its block equivalent and remainder, then adds them to the player's inventory.
 *
 * @param block The material representing the block form of the item.
 * @param ratio The conversion ratio of items to blocks (default 9)
 */
fun ItemStack.inBlocks(block: ItemStack, ratio: Int = 9): MutableList<ItemStack> {
    val inventory = mutableListOf<ItemStack>()
    if (amount <= 0) return inventory

    val blocks = amount / ratio
    val remainder = amount % ratio

    if (blocks > 0) {
        val blockStack = block.also { it.amount = amount }
        inventory +=  blockStack
    }

    if (remainder > 0) {
        val itemStack = this.also { it.amount = remainder }
        inventory += itemStack
    }
    return inventory
}

fun ItemStack.isOf(itemStack: ItemStack): Boolean {
    return this.type == itemStack.type && this.itemMeta == itemStack.itemMeta
}