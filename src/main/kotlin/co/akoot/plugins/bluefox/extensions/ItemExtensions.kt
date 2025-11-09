package co.akoot.plugins.bluefox.extensions

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.economy.Coin
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
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

fun ItemStack.isOf(vararg itemStack: ItemStack?): Boolean {
    for(item in itemStack) {
        if(item == null) continue
        if(this.type == item.type && ((!this.hasItemMeta() && !item.hasItemMeta()) || (this.itemMeta == item.itemMeta))) {
            return true
        }
    }
    return false
}

fun ItemStack.isCoin(coin: Coin): Boolean {
    if(coin.backing == null) return false
    if(this.type == coin.backing.type || this.type == coin.backingBlock?.type) {
        if(coin.backing.hasItemMeta()) {
            return isOf(coin.backing, coin.backingBlock)
        }
        return true
    }
    return false
}

fun ItemStack.withAmount(amount: Int): ItemStack {
    this.amount = amount
    return this
}

fun ItemStack.withDisplayName(component: Component): ItemStack {
    val meta = itemMeta
    meta.displayName(component)
    setItemMeta(meta)
    return this
}

fun ItemStack.withLore(vararg component: Component): ItemStack {
    val meta = itemMeta
    meta.lore(component.toList())
    setItemMeta(meta)
    return this
}

fun ItemStack.withAddedLore(vararg component: Component): ItemStack {
    val meta = itemMeta
    val lore = meta.lore()?.toMutableList() ?: mutableListOf()
    lore.addAll(component)
    meta.lore(lore)
    setItemMeta(meta)
    return this
}

inline fun<reified T: Any> ItemStack.withPDC(key: NamespacedKey, value: T): ItemStack {
    val meta = itemMeta
    meta.setPDC(key, value)
    setItemMeta(meta)
    return this
}