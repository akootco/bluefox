package co.akoot.plugins.bluefox.api.economy

import org.bukkit.Material

open class Coin(val id: Int, val ticker: String, val name: String, val description: String, val backing: Material = Material.AIR) {
    companion object {
        var DIA = Coin(1, "DIA", "Diamond", "Diamond Coin", Material.DIAMOND)
        var NTRI = Coin(2, "NTRI", "Netherite", "Netherite Coin", Material.NETHERITE_INGOT)
    }
}