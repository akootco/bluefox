package co.akoot.plugins.bluefox.extensions

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.FoxConfig
import co.akoot.plugins.bluefox.api.Kolor
import co.akoot.plugins.bluefox.api.Profile
import co.akoot.plugins.bluefox.api.economy.Wallet
import co.akoot.plugins.bluefox.util.Text
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.geysermc.api.Geyser
import java.io.File
import java.util.*

val OfflinePlayer.defaultWalletAddress: String get() = this.uniqueId.toString().replace("-", "")

fun OfflinePlayer.getDataFile(): File {
    return File("world/playerdata/$uniqueId.dat")
}

val OfflinePlayer.configFile: File get() = File("users/$uniqueId.conf")
val OfflinePlayer.config: FoxConfig get() = FoxConfig(configFile)
val Player.config: FoxConfig get() = BlueFox.instance.configs[name] ?: BlueFox.instance.registerConfig(name, configFile.path)

val OfflinePlayer.profile: Profile
    get() = Profile(this.uniqueId)

fun List<OfflinePlayer>.names(): List<String> {
    return this.mapNotNull { it.name }
}

val Player.isBedrock: Boolean get() = BlueFox.geyser?.isBedrockPlayer(uniqueId) ?: false

val OfflinePlayer.wallet: Wallet? get() = Wallet.playerWallets[this]

val Player.isSurventure: Boolean get() = gameMode in setOf(GameMode.SURVIVAL, GameMode.ADVENTURE) // :)

var Player.language: String?
    get() = getPDC<String>(BlueFox.instance.key("language"))
    set(value) = setPDC(BlueFox.instance.key("language"), value)

/**
 * Counts the total amount of a specified item and the equivalent amount of a specified block
 * in the player's inventory. The block count is multiplied by a given ratio.
 *
 * @param item the material type of the item to count
 * @param block the material type of the block to count, which contributes to the total based on the given ratio
 * @param ratio the multiplier applied to the block count, defaults to 9
 * @return the total count of the specified item and adjusted count of the specified block in the player's inventory
 */
fun Player.countIncludingBlocks(item: Material, block: Material, ratio: Int = 9): Int {
    var total = 0
    for (itemStack in inventory.contents) {
        if (itemStack == null) continue
        total += when (itemStack.type) {
            item -> itemStack.amount
            block -> itemStack.amount * ratio
            else -> continue
        }
    }
    return total
}

/**
 * Removes a specified number of items from the player inventory, starting with items and then
 * including blocks converted into items based on the specified ratio.
 *
 * @param item the material type of the item to count
 * @param block the material type of the block to count, which contributes to the total based on the given ratio
 * @param ratio the multiplier applied to the block count, defaults to 9
 * @return whether the specified number of items and blocks were removed successfully
 */
fun Player.removeIncludingBlocks(item: Material, block: Material, amount: Int? = null, ratio: Int = 9): Boolean {
    val total = countIncludingBlocks(item, block, ratio)
    var remaining = amount

    // remove all if the amount is null
    if(amount == null) {
        return inventory.removeAll { it != null && it.type in setOf(item, block) }
    } else if(amount > total) {
        return false // not enough items!
    }

    // remove items first
    for(slot in 0 until inventory.size) {
        val itemStack = inventory.getItem(slot) ?: continue
        if(itemStack.type == item) {
            val remove = minOf(itemStack.amount, remaining)
            itemStack.amount -= remove
            remaining -= remove
            if(remaining <= 0) return true
        }
    }

    // remove blocks if necessary
    for(slot in 0 until inventory.size) {
        val itemStack = inventory.getItem(slot) ?: continue
        if(itemStack.type == block) {
            val items = itemStack.amount * ratio
            if(items <= 0) continue

            val neededItems = remaining
            val neededBlocks = (neededItems + (ratio - 1)) / ratio

            val removeBlocks = minOf(itemStack.amount, neededBlocks)
            val remove = removeBlocks * ratio

            itemStack.amount -= removeBlocks
            remaining -= remove

            if (itemStack.amount <= 0) inventory.setItem(slot, null)

            // give back leftovers
            if(remaining <= 0) {
                val extra = -remaining
                if (extra > 0) {
                    inventory.addItem(ItemStack(item, extra))
                }
                return true
            }
        }
    }

    return true
}

/**
 * Gives items to the player's inventory in block form and remainder items based on a specified ratio.
 * Converts the given amount of an item to its block equivalent and remainder, then adds them to the player's inventory.
 *
 * @param item The material representing the item to convert and give as a remainder.
 * @param block The material representing the block form of the item.
 * @param amount The total amount of the item to be distributed between blocks and remainder.
 * @param ratio The conversion ratio of items to blocks (default 9)
 */
fun Player.giveInBlocks(item: Material, block: Material, amount: Int, ratio: Int = 9) {
    if (amount <= 0) return

    val blocks = amount / ratio
    val remainder = amount % ratio

    if (blocks > 0) {
        val blockStack = ItemStack(block, blocks)
        inventory.addItem(blockStack)
    }

    if (remainder > 0) {
        val itemStack = ItemStack(item, remainder)
        inventory.addItem(itemStack)
    }
}

fun Player.sendTitle(text: Text, subtitle: Text? = null, fadeIn: Double = 0.5, stay: Double = 3.0, fadeOut: Double = 0.5) {
    text.sendTitle(this, subtitle, fadeIn, stay, fadeOut)
}

fun Player.sendSubtitle(text: Text, fadeIn: Double = 0.5, stay: Double = 3.0, fadeOut: Double = 0.5) {
    text.sendSubtitle(this, fadeIn, stay, fadeOut)
}

fun Player.sendActionBar(text: Text) {
    sendActionBar(text.component)
}
//
//val OfflinePlayer.displayName: TextComponent get() = getPDC<String>(BlueFox.instance.key("nickname"))?.let {
//
//} ?: Component.text(name ?: uniqueId.toString())