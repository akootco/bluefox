package co.akoot.plugins.bluefox.commands

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.FoxCommand
import co.akoot.plugins.bluefox.api.Kolor
import co.akoot.plugins.bluefox.api.economy.Coin
import co.akoot.plugins.bluefox.api.economy.Economy
import co.akoot.plugins.bluefox.api.economy.Market
import co.akoot.plugins.bluefox.api.economy.Wallet
import co.akoot.plugins.bluefox.extensions.wallet
import co.akoot.plugins.bluefox.util.Text
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.command.CommandSender

class MarketCommand(plugin: BlueFox): FoxCommand(plugin, "market", aliases = arrayOf("price", "prices")) {

    private val amountPresets = mutableListOf("1", "5", "10", "25", "50", "100", "250", "500", "1000")

    override fun onTabComplete(
        sender: CommandSender,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        if(alias == "prices") return mutableListOf()
        if(alias != id) return onTabComplete(sender, id, arrayOf(alias) + args)
        return when (args.size) {
            1 -> mutableListOf("price", "info", "print").permissionCheck(sender)
            2 -> {
                when(args[0]) {
                    "price", "info" -> Market.coins.keys.toMutableList()
                    "print" -> amountPresets
                    else -> mutableListOf()
                }
            }
            3 -> {
                when(args[0]) {
                    "price", "info" -> Market.coins.keys.minus(args[1]).toMutableList()
                    "print" -> Market.coins.keys.toMutableList()
                    else -> mutableListOf()
                }
            }
            else -> mutableListOf()
        }
    }

    override fun onCommand(
        sender: CommandSender,
        alias: String,
        args: Array<out String>
    ): Boolean {
        if(args.isEmpty()) return onCommand(sender, "market", arrayOf("price"))
        if(alias == "prices") return onCommand(sender, "market", arrayOf("price"))
        if(alias != id) return onCommand(sender, id, arrayOf(alias) + args)
        val coin1 = args.getOrNull(1)?.let { Market.getCoin(it) }
        val coin2 = args.getOrNull(2)?.let { Market.getCoin(it) }
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
            "print" -> {
                permissionCheck(sender, "print") ?: return false
                val player = getPlayerSender(sender).getAndSend(sender) ?: return false
                val wallet = player.wallet
                if (wallet == null) {
                    Text(sender) {
                        Kolor.ERROR("This sucks! You have no wallet...")
                    }
                    return false
                }
                val amount = runCatching{args[1].toBigDecimalOrNull()}.getOrNull()
                if (amount == null) {
                    Text(sender) {
                        Kolor.ERROR("HOW MANY?????!!!!!!")
                    }
                    return false
                }
                if (coin2 == null) {
                    Text(sender) {
                        Kolor.ERROR("Brother, what coin...")
                    }
                    return false
                }
                val result = Wallet.BANK.send(wallet, coin2, amount)
                if (result < 0) {
                    Text(sender) {
                        Kolor.ERROR("ERM........")
                    }
                    return false
                }
                Economy.sendBalance(sender, wallet, coin2)
            }
        }
        return true
    }
}