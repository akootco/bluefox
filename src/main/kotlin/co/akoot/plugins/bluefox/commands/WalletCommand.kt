package co.akoot.plugins.bluefox.commands

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.FoxCommand
import co.akoot.plugins.bluefox.api.Kolor
import co.akoot.plugins.bluefox.api.economy.Coin
import co.akoot.plugins.bluefox.api.economy.Economy
import co.akoot.plugins.bluefox.api.economy.Economy.Error.BUYER_MISSING_COIN
import co.akoot.plugins.bluefox.api.economy.Economy.Error.COIN_HAS_NO_BACKING
import co.akoot.plugins.bluefox.api.economy.Economy.Error.INSUFFICIENT_BALANCE
import co.akoot.plugins.bluefox.api.economy.Economy.Error.INSUFFICIENT_BUYER_BALANCE
import co.akoot.plugins.bluefox.api.economy.Economy.Error.INSUFFICIENT_ITEMS
import co.akoot.plugins.bluefox.api.economy.Economy.Error.INSUFFICIENT_SELLER_BALANCE
import co.akoot.plugins.bluefox.api.economy.Economy.Error.INVALID_GAME_MODE
import co.akoot.plugins.bluefox.api.economy.Economy.Error.INVALID_WORLD
import co.akoot.plugins.bluefox.api.economy.Economy.Error.MISSING_COIN
import co.akoot.plugins.bluefox.api.economy.Economy.Error.NUMBER_TOO_SMALL
import co.akoot.plugins.bluefox.api.economy.Economy.Error.PRICE_UNAVAILABLE
import co.akoot.plugins.bluefox.api.economy.Economy.Error.SELLER_MISSING_COIN
import co.akoot.plugins.bluefox.api.economy.Economy.Error.SQL_ERROR
import co.akoot.plugins.bluefox.api.economy.Market
import co.akoot.plugins.bluefox.api.economy.Wallet
import co.akoot.plugins.bluefox.extensions.countIncludingBlocks
import co.akoot.plugins.bluefox.extensions.invoke
import co.akoot.plugins.bluefox.extensions.wallet
import co.akoot.plugins.bluefox.util.Text
import co.akoot.plugins.bluefox.util.Text.Companion.component
import co.akoot.plugins.bluefox.util.Text.Companion.plus
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.math.round

class WalletCommand(plugin: BlueFox) : FoxCommand(plugin, "wallet", aliases = arrayOf("balance", "bal", "send", "request", "withdraw", "deposit", "swap")) {

    private val amountPresets = mutableListOf("all", "1", "2", "5", "10", "20", "25", "50", "100", "250", "500", "1000", "2000", "5000")

    override fun onTabComplete(
        sender: CommandSender,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        if(alias != id) return onTabComplete(sender, id, arrayOf(alias) + args)
        return when (args.size) {
            1 -> mutableListOf("deposit", "withdraw", "balance", "swap", "send", "request")
            2 -> {
                when (args[0]) {
                    "send" -> getOfflinePlayerSuggestions(args, setOf("@" + sender.name), prefix = "@")
                    "request" -> getOnlinePlayerSuggestions(args, setOf("@" + sender.name), prefix = "@")
                    "balance", "bal" -> Market.coins.keys.toMutableList()
                    else -> amountPresets
                }
            }

            3 -> {
                if (args[0] in setOf("send", "request")) {
                    val target = args[1]
                    if (target.startsWith("@")) {
                        getOfflinePlayer(args[1].substring(1)).value ?: return mutableListOf()
                    } else if (target.startsWith("0x")) {
                        if (target.length != 34) return mutableListOf()
                    } else {
                        return mutableListOf()
                    }
                    return amountPresets.minus("all").toMutableList()
                } else if(args[0] == "balance" || args[0] == "bal") {
                    return getOfflinePlayerSuggestions(args, setOf("@" + sender.name), prefix = "@")
                }
                when (args[0]) {
                    "deposit", "withdraw" -> Market.coins.entries.filter { it.value.backing != Material.AIR }
                        .map { it.key }.toMutableList()

                    "swap" -> Market.coins.keys.toMutableList()
                    else -> mutableListOf()
                }
            }

            4 -> {
                return if (args[0] == "swap") {
                    Market.coins[args[2]] ?: return mutableListOf()
                    Market.coins.keys.minus(args[2].uppercase()).toMutableList()
                } else if (args[0] in setOf("send", "request")) {
                    Market.coins.keys.toMutableList()
                } else  mutableListOf()
            }

            else -> mutableListOf()
        }
    }

    override fun onCommand(
        sender: CommandSender,
        alias: String,
        args: Array<out String>
    ): Boolean {
        if(alias != id) return onCommand(sender, id, arrayOf(alias) + args)
        val player = getPlayerSender(sender).getAndSend(sender) ?: return false
        val wallet = player.wallet
        if (wallet == null) {
            Text(sender) {
                Kolor.ERROR("This sucks! You have no wallet...")
            }
            return false
        }
        if (args.isEmpty()) {
            Economy.sendWallet(sender, wallet, true)
            return true
        }
        val action = args[0]
        when (action) {
            "swap" -> {
                val coin1 = runCatching { Market.coins[args[2].uppercase()] }.getOrNull() ?: return false
                val amount = args[1].toDoubleOrNull() ?: wallet.balance[coin1]
                if (amount == null) {
                    Text(sender) {
                        Kolor.ERROR("Erm? How much ") + Kolor.ERROR.accent(coin1.toString()) + " would you like to swap..?"
                    }
                    return false
                } else if(amount <= 0) {
                    Text(sender) {
                        Kolor.TEXT("The server glitched and deposited ") + round(Math.random() * 10000) + Kolor.ACCENT(" $coin1") + " to your wallet."
                    }
                    return false
                }
                val coin2 = runCatching { Market.coins[args[3].uppercase()] }.getOrNull() ?: return false
                if (coin1 == coin2) {
                    Text(sender) {
                        Kolor.ERROR("FOOL! You can't swap for the same coin!")
                    }
                    return false
                }
                Text(sender) {
                    val price = Market.prices[coin2 to coin1] ?: 0.0
                    when (wallet.swap(coin1, coin2, amount)) {
                        BUYER_MISSING_COIN, INSUFFICIENT_BUYER_BALANCE -> Kolor.ERROR("Erm......Idk what to say but that just didn't work.")
                        INSUFFICIENT_SELLER_BALANCE, SELLER_MISSING_COIN -> Kolor.ERROR("You don't have enough ") + Kolor.ERROR.accent(
                            coin1.toString()
                        ) + " to swap!"

                        PRICE_UNAVAILABLE -> Kolor.ERROR("No price is set for this trade!")
                        else -> {
                            Kolor.ALT("Swapped ") + amount + Kolor.ACCENT(" $coin1") + Kolor.TEXT(" for ") + amount * price + Kolor.ACCENT(" $coin2") + "."
                        }
                    }
                }
            }

            "balance", "bal" -> {
                val coin = runCatching { Market.coins[args[1].uppercase()] }.getOrNull()
                val targetArg = args.getOrNull(2)

                val targetWallet = when {
                    targetArg == null -> wallet
                    targetArg.startsWith("@") -> {
                        val targetPlayer = getOfflinePlayer(targetArg.substring(1)).getAndSend(sender) ?: return false
                        targetPlayer.wallet ?: Wallet.create(targetPlayer) ?: wallet
                    }
                    else -> Wallet.get(targetArg.substring(2)) ?: wallet
                }

                targetWallet.load()

                if (coin == null) {
                    Economy.sendWallet(sender, targetWallet, targetWallet == wallet)
                } else {
                    Economy.sendBalance(sender, targetWallet, coin)
                }

            }

            "deposit", "withdraw" -> {
                val amountString = args.getOrNull(1) ?: "all"
                var coin = runCatching { Market.coins[args[2].uppercase()] }.getOrNull()
                if(coin == null && amountString == "all") {
                    var success = false
                    for ((key, coin) in Market.coins) {
                        if (coin.backing == null) continue
                        if(action == "withdraw" && ((wallet.balance[coin] ?: 0.0) < 1.0)) continue
                        if(action == "deposit") {
                            val count = if(coin.backingBlock != null) {
                                player.countIncludingBlocks(coin.backing, coin.backingBlock)
                            } else {
                                player.inventory.sumOf { it?.amount ?: 0 }
                            }
                            if(count < 1) continue
                        }
                        success = true
                        val newArgs = args.toMutableList()
                        if(args.size == 1) newArgs.add("all")
                        newArgs.add(key)
                        onCommand(sender, alias, newArgs.toTypedArray())
                    }
                    if(!success) {
                        Text(sender) {
                            Kolor.WARNING("You are broke!!!!")
                        }
                    }
                    return true
                }
                if(coin == null) coin  = Coin.DIA
                val amount = if (amountString == "all") {
                    if (action == "deposit") {
                        if(coin.backing != null) {
                            if(coin.backingBlock != null) {
                                player.countIncludingBlocks(coin.backing, coin.backingBlock).toDouble()
                            } else {
                                player.inventory.sumOf { it?.amount ?: 0 }.toDouble()
                            }
                        } else null
                    } else {
                        wallet.balance[coin] ?: 0.0
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

                        INSUFFICIENT_ITEMS -> Kolor.ERROR("You don't have enough ") + Kolor.ERROR.accent(coin.backing!!.component) + Kolor.ERROR(
                            " to complete this transaction!"
                        )

                        INVALID_GAME_MODE -> Kolor.ERROR("You can't $action in this game mode!")
                        INVALID_WORLD -> Kolor.ERROR("You can't $action in this world!")

                        SQL_ERROR -> Kolor.ERROR("Erm... Something blew up and it's all my fault!")
                        else -> {
                            Kolor.ALT("Nice $action! ") + Kolor.TEXT("You now have ") + (wallet.balance[coin]
                                ?: 0.0) + Kolor.ACCENT(" $coin") + "."
                        }
                    }
                }
            }

            "send", "request" -> {
                val actionText = if (action == "send") "to" else "from"
                var targetPlayer: Player? = null
                val targetWallet = if (args[1].startsWith("@")) {
                    if (action == "send") {
                        val target = getOfflinePlayer(args[1].substring(1)).getAndSend(sender) ?: return false
                        targetPlayer = target.player
                        target.wallet ?: Wallet.create(target)
                    } else {
                        targetPlayer = getPlayer(args[1].substring(1)).getAndSend(sender) ?: return false
                        targetPlayer.wallet ?: Wallet.create(targetPlayer)
                    }
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
                        Kolor.ERROR("You can't $action money $actionText yourself!")
                    }
                    return false
                }
                val coin = runCatching { Market.coins[args[3].uppercase()] }.getOrNull() ?: Coin.DIA
                val amount = runCatching {
                    args[2].run {
                        if (this == "all") wallet.balance[coin]
                        else toDouble()
                    }
                }.getOrNull()
                if (amount == null) {
                    Text(sender) {
                        Kolor.ERROR("Erm? How much ") + Kolor.ERROR.accent(coin.toString()) + Kolor.ERROR(" would you like to $action $actionText ") + Kolor.ERROR.alt(
                            args[1]
                        ) + "..?"
                    }
                    return false
                } else if (amount < 0) {
                    Text(sender) {
                        Kolor.TEXT("The server glitched and deposited ") + round(Math.random() * 10000) + Kolor.ACCENT(" $coin") + " to your wallet."
                    }
                    return false
                } else if (amount == 0.0) {
                    Text(sender) {
                        if (action == "request") {
                            Kolor.ERROR("Poor ") + Kolor.ERROR.alt(args[1]) + Kolor.ERROR(" hasn't any ") + Kolor.ERROR.accent(coin.toString()) + Kolor.ERROR(".")
                        } else {
                            Kolor.ERROR("You haven't any ") + Kolor.ERROR.accent(coin.toString()) + Kolor.ERROR("...")
                        }
                    }
                    return false
                }
                if (action == "request") {
                    if (targetPlayer?.player == null) {
                        Text(sender) {
                            Kolor.ERROR("Literally who?")
                        }
                        return false
                    } else if ((targetWallet.balance[coin] ?: 0.0) < amount) {
                        Text(sender) {
                            Kolor.ALT(args[1]) + Kolor.ERROR(" doesn't have enough ") + Kolor.ERROR.accent(coin.toString()) + " to spare!"
                        }
                        return false
                    }
                    Text(sender) {
                        Kolor.TEXT("Requested ") + amount + Kolor.ACCENT(" $coin") + " from " + Kolor.ALT(args[1]) + "."
                    }
                    Text(targetPlayer) {
                        Kolor.ALT("@${sender.name}") + Kolor.TEXT(" is requesting ") + amount + Kolor.ACCENT(" $coin") + Kolor.TEXT(" from you!\n") +
                                Kolor.ALT.accent("(Click here to send)")
                                    .suggest("/wallet send @${sender.name} $amount ${coin.ticker}")
                    }
                    return true
                }
                Text(sender) {
                    when (wallet.send(targetWallet, coin, amount)) {
                        MISSING_COIN, INSUFFICIENT_BALANCE -> Kolor.ERROR("You do not even have any ") + Kolor.ERROR.accent(
                            coin.toString()
                        ) + Kolor.ERROR("!")

                        SQL_ERROR -> Kolor.ERROR("Sorry to break it to you but that just didn't work!")
                        else -> {
                            Text(targetPlayer) {
                                (Kolor.ALT("@${sender.name}") + Kolor.TEXT(" sent you ") + amount + Kolor.ACCENT(" $coin") + Kolor.TEXT("!")).execute("/wallet balance")
                            }
                            (Kolor.TEXT("Sent ") + amount + Kolor.ACCENT(" $coin") + " to " + Kolor.ALT(args[1]) + ".").execute("/wallet balance")
                        }
                    }
                }
            }
        }
        return true
    }
}