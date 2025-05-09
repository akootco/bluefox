package co.akoot.plugins.bluefox.api.economy

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import org.bukkit.Material

open class Coin(val id: Int, val ticker: String, val name: String, val description: String, val backing: Material = Material.AIR) {

    companion object {
        private val DEFAULT_DIA = Coin(1, "DIA", "Diamond", "Diamond Coin", Material.DIAMOND)
        private val DEFAULT_NTRI = Coin(2, "NTRI", "Netherite", "Netherite Coin", Material.NETHERITE_INGOT)
        val DIA: Coin get() = Market.coins["DIA"] ?: DEFAULT_DIA
        val NTRI: Coin get() = Market.coins["NTRI"] ?: DEFAULT_NTRI
    }

    override fun toString(): String {
        return "$$ticker"
    }
}