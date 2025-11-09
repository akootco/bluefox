package co.akoot.plugins.bluefox.api.economy

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.extensions.itemStack
import co.akoot.plugins.bluefox.extensions.withDisplayName
import co.akoot.plugins.bluefox.extensions.withPDC
import co.akoot.plugins.bluefox.util.Text
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

open class Coin(
    val id: Int,
    val ticker: String,
    val name: String,
    val description: String,
    val backing: ItemStack? = null,
    val backingBlock: ItemStack? = null,
    val backingBlockValue: Int = 9,

) {

    companion object {
        private val DEFAULT_DIA = Coin(1, "DIA", "Diamond", "Diamond Coin", Material.DIAMOND.itemStack, Material.DIAMOND_BLOCK.itemStack)
        private val DEFAULT_NTRI = Coin(2, "NTRI", "Netherite", "Netherite Coin", Material.NETHERITE_INGOT.itemStack, Material.NETHERITE_BLOCK.itemStack)
        private val DEFAULT_AD = Coin(3, "AD", "Ancient Debris", "Ancient Debris Coin", Material.ANCIENT_DEBRIS.itemStack)
        private val DEFAULT_AMETHYST = Coin(4, "AMETHYST", "Amethyst", "Amethyst Coin", Material.AMETHYST_SHARD.itemStack.withDisplayName(
            Text($$"$AMETHYST").color(0xa788f7).italic(false).component
        ).withPDC(BlueFox.key("ticker"), "AMETHYST"), Material.AMETHYST_BLOCK.itemStack, 4)
        val DIA: Coin get() = Market.coins["DIA"] ?: DEFAULT_DIA
        val NTRI: Coin get() = Market.coins["NTRI"] ?: DEFAULT_NTRI
        val AD: Coin get() = Market.coins["AD"] ?: DEFAULT_AD
        val AMETHYST: Coin get() = Market.coins["AMETHYST"] ?: DEFAULT_AMETHYST
    }

    override fun toString(): String {
        return "$$ticker"
    }
}