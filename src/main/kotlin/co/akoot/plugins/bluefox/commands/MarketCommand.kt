package co.akoot.plugins.bluefox.commands

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.FoxCommand
import co.akoot.plugins.bluefox.api.Kolor
import co.akoot.plugins.bluefox.api.economy.Coin
import co.akoot.plugins.bluefox.api.economy.Economy
import co.akoot.plugins.bluefox.api.economy.Market
import co.akoot.plugins.bluefox.util.Text
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.command.CommandSender

class MarketCommand(plugin: BlueFox): FoxCommand(plugin, "market") {
    override fun onTabComplete(
        sender: CommandSender,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        return when (args.size) {
            1 -> mutableListOf("price", "info")
            2 -> Market.coins.keys.toMutableList()
            3 -> Market.coins.keys.minus(args[1]).toMutableList()
            else -> mutableListOf()
        }
    }

    override fun onCommand(
        sender: CommandSender,
        alias: String,
        args: Array<out String>
    ): Boolean {
        val coin1 = runCatching { Market.coins[args[1]] }.getOrNull()
        val coin2 = runCatching { Market.coins[args[2]] }.getOrNull()
        when (args[0]) {
            "price" -> {
                if (coin1 == null) {
                    for(coin in Market.coins.values) {
                        Economy.sendPrice(sender, coin)
                    }
                } else if(coin2 == null) {
                    Economy.sendPrice(sender, coin1)
                } else {
                    Economy.sendPrice(sender, coin1, coin2)
                }
            }
            "info" -> {
                if (coin1 == null) {
                    for(coin in Market.coins.values) {
                        Economy.sendInfo(sender, coin)
                    }
                } else {
                    Economy.sendInfo(sender, coin1)
                    if(coin2 != null) {
                        Economy.sendInfo(sender, coin2)
                    }
                }
            }
        }
        return true
    }
}