package co.akoot.plugins.bluefox.commands

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.FoxCommand
import co.akoot.plugins.bluefox.api.Kolor
import co.akoot.plugins.bluefox.api.economy.Coin
import co.akoot.plugins.bluefox.api.economy.Economy.Error.COIN_HAS_NO_BACKING
import co.akoot.plugins.bluefox.api.economy.Economy.Error.INSUFFICIENT_BALANCE
import co.akoot.plugins.bluefox.api.economy.Economy.Error.INSUFFICIENT_ITEMS
import co.akoot.plugins.bluefox.api.economy.Economy.Error.MISSING_COIN
import co.akoot.plugins.bluefox.api.economy.Economy.Error.NUMBER_TOO_SMALL
import co.akoot.plugins.bluefox.api.economy.Economy.Error.SQL_ERROR
import co.akoot.plugins.bluefox.api.economy.Market
import co.akoot.plugins.bluefox.api.economy.Wallet
import co.akoot.plugins.bluefox.extensions.invoke
import co.akoot.plugins.bluefox.extensions.wallet
import co.akoot.plugins.bluefox.util.Text
import co.akoot.plugins.bluefox.util.Text.Companion.component
import co.akoot.plugins.bluefox.util.Text.Companion.plus
import co.akoot.plugins.bluefox.util.Text.Companion.text
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.math.absoluteValue
import kotlin.math.round

class WalletCommand(plugin: BlueFox) : FoxCommand(plugin, "wallet") {

    private val amountPresets = mutableListOf("all", "1", "5", "10", "25", "50", "100", "250", "500", "1000")

    override fun onTabComplete(
        sender: CommandSender,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        return when(args.size) {
            1 -> mutableListOf("deposit", "withdraw", "balance", "swap", "send")
            2 -> {
                if (args[0] == "send") getOfflinePlayerSuggestions(args, setOf("@" + sender.name), prefix = "@")
                else if (args[0] == "balance") Market.coins.keys.toMutableList()
                else amountPresets
            }
            3 -> {
                if (args[0] == "send") {
                    val target = args[1]
                    if (target.startsWith("@")) {
                        getOfflinePlayer(args[1].substring(1)).value ?: return mutableListOf()
                    } else if(target.startsWith("0x")) {
                        if (target.length != 34) return mutableListOf()
                    } else {
                        return mutableListOf()
                    }
                    return amountPresets
                }
                when(args[0]) {
                   "deposit", "withdraw" -> Market.coins.entries.filter{ it.value.backing != Material.AIR }.map { it.key }.toMutableList()
                    "swap" -> Market.coins.keys.toMutableList()
                    else -> mutableListOf()
                }
            }
            4 -> {
                if (args[0] == "swap") {
                    Market.coins[args[2]] ?: return mutableListOf()
                    return Market.coins.keys.minus(args[2]).toMutableList()
                }
                else if (args[0] == "send") {
                    return Market.coins.keys.toMutableList()
                }
                mutableListOf()
            }
            else -> mutableListOf()
        }
    }

    override fun onCommand(
        sender: CommandSender,
        alias: String,
        args: Array<out String>
    ): Boolean {
        val player = getPlayerSender(sender).getAndSend(sender) ?: return false
        val wallet = player.wallet
        if (wallet == null) {
            Text(sender) {
                Kolor.ERROR("This sucks! You have no wallet...")
            }
            return false
        }
        if(args.isEmpty()) {
            sendWallet(sender, wallet)
            return true
        }
        val action = args[0]
        when (action) {
            "balance" -> {
                val coin = runCatching { Market.coins[args[1]] }.getOrNull()
                if (coin == null) {
                    sendWallet(sender, wallet)
                } else {
                    sendBalance(sender, wallet, coin)
                }
            }
            "deposit", "withdraw" -> {
                val coin = runCatching { Market.coins[args[2]] }.getOrNull() ?: Coin.DIA
                val amountString = args.getOrNull(1) ?: "all"
                val amount = if(amountString == "all") {
                    if(action == "deposit") {
                        var count = 0.0
                        player.inventory.forEach {
                            if (it != null && it.type == coin.backing) {
                                count += it.amount
                            }
                        }
                        count
                    } else {
                        wallet.balance[coin] ?: 0.00
                    }
                } else {
                    amountString.toDoubleOrNull()
                }
                if (amount == null) {
                    Text(sender) {
                        Kolor.ERROR("Erm? How much $coin would you like to ${if (action == "deposit") "deposit" else "withdraw"}..?")
                    }
                    return false
                } else if (amount < 0) {
                    Text(sender) {
                        Kolor.TEXT("The server glitched and deposited ") + round(Math.random() * 10000) + Kolor.ACCENT(" $coin") + " to your wallet."
                    }
                    return false
                }
                val result = if (action == "deposit") { //doing another check like this? what are you some kinda noob!
                    wallet.deposit(player, coin, amount.toInt())
                } else {
                    wallet.withdraw(player, coin, amount.toInt())
                }
                Text(sender) {
                    when (result) {
                        NUMBER_TOO_SMALL -> Kolor.ERROR("You can't $action less than 1 ") + Kolor.ERROR.accent(coin.toString()) + "!"
                        COIN_HAS_NO_BACKING -> Kolor.ERROR.alt(coin.toString()) + Kolor.ERROR(" doesn't have a backing item!")
                        INSUFFICIENT_BALANCE -> Kolor.ERROR("You don't have enough ") + Kolor.ERROR.accent(coin.toString()) + Kolor.ERROR(
                            " to complete this transaction!"
                        )

                        INSUFFICIENT_ITEMS -> Kolor.ERROR("You don't have enough ") + Kolor.ERROR.accent(coin.backing.component) + Kolor.ERROR(
                            " to complete this transaction!"
                        )

                        SQL_ERROR -> Kolor.ERROR("Erm... Something blew up and it's all my fault!")
                        else -> {
                            Kolor.ALT("Nice $action! ") + Kolor.TEXT("You now have ") + (wallet.balance[coin]
                                ?: 0.0) + Kolor.ACCENT(" $coin") + "."
                        }
                    }
                }
            }
            "send" -> {
                var targetPlayer: Player? = null
                val targetWallet = if(args[1].startsWith("@")) {
                    val target = getOfflinePlayer(args[1].substring(1)).getAndSend(sender) ?: return false
                    targetPlayer = target.player
                    target.wallet ?: Wallet.create(target)
                } else {
                    Wallet.get(args[1].substring(2))
                }
                if (targetWallet == null) {
                    Text(sender) {
                        Kolor.ERROR("That wallet does not exist, and I am NOT about to make it either!")
                    }
                    return false
                }
                if (wallet == targetWallet) {
                    Text(sender) {
                        Kolor.ERROR("You can't send money to yourself!")
                    }
                    return false
                }
                val amount = args[2].toDoubleOrNull()
                val coin = runCatching { Market.coins[args[3]] }.getOrNull() ?: Coin.DIA
                if (amount == null) {
                    Text(sender) {
                        Kolor.ERROR("Erm? How much $coin would you like to send to $targetWallet..?")
                    }
                    return false
                }
                Text(sender) {
                    when(wallet.send(targetWallet, coin, amount)) {
                        MISSING_COIN, INSUFFICIENT_BALANCE -> Kolor.ERROR("You do not even have any ") + Kolor.ERROR.accent(coin.toString()) + Kolor.ERROR("!")
                        SQL_ERROR -> Kolor.ERROR("Sorry to break it to you but that just didn't work!")
                        else -> {
                            targetPlayer?.sendMessage {
                                sender.name() + Kolor.TEXT(" sent you ") + amount + Kolor.ACCENT(" $coin") + "!"
                            }
                            Kolor.TEXT("Sent ") + amount + Kolor.ACCENT(" $coin") + " to " + Kolor.ALT(args[1]) + "."
                        }
                    }
                }
            }
        }
        return true
    }

    private fun sendWallet(sender: CommandSender, wallet: Wallet) {
        for(coin in wallet.balance.keys) {
            sendBalance(sender, wallet, coin)
        }
    }

    private fun sendBalance(sender: CommandSender, wallet: Wallet, coin: Coin) {
        Text(sender) {
            Kolor.ACCENT("$coin: ") + (wallet.balance[coin] ?: 0.0)
        }
    }
}