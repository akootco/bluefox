package co.akoot.plugins.bluefox.commands

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.FoxCommand
import co.akoot.plugins.bluefox.api.Kolor
import co.akoot.plugins.bluefox.api.economy.Coin
import co.akoot.plugins.bluefox.api.economy.Economy
import co.akoot.plugins.bluefox.api.economy.Economy.Error.BUYER_MISSING_COIN
import co.akoot.plugins.bluefox.api.economy.Economy.Error.COIN_HAS_NO_BACKING
import co.akoot.plugins.bluefox.api.economy.Economy.Error.EVENT_CANCELLED
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
import co.akoot.plugins.bluefox.api.economy.Economy.isMoreThanZero
import co.akoot.plugins.bluefox.api.economy.Economy.rounded
import co.akoot.plugins.bluefox.api.economy.Market
import co.akoot.plugins.bluefox.api.economy.Wallet
import co.akoot.plugins.bluefox.api.events.PlayerRequestCoinEvent
import co.akoot.plugins.bluefox.api.events.PlayerRequestTradeEvent
import co.akoot.plugins.bluefox.extensions.countIncludingBlocks
import co.akoot.plugins.bluefox.extensions.invoke
import co.akoot.plugins.bluefox.extensions.wallet
import co.akoot.plugins.bluefox.util.Text
import co.akoot.plugins.bluefox.util.Text.Companion.component
import co.akoot.plugins.bluefox.util.Text.Companion.plus
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.math.BigDecimal
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
            1 -> mutableListOf("deposit", "withdraw", "balance", "swap", "send", "request").permissionCheck(sender)
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
        permissionCheck(sender, action) ?: return false
        when (action) {
            "swap" -> {
                val coin1 = args.getOrNull(2)?.let { Market.getCoin(it) } ?: return false
                val amount = args[1].toBigDecimalOrNull() ?: wallet.balance[coin1]
                if (amount == null) {
                    Text(sender) {
                        Kolor.ERROR("Erm? How much ") + Kolor.ERROR.accent(coin1.toString()) + " would you like to swap..?"
                    }
                    return false
                } else if(!amount.isMoreThanZero) {
                    Text(sender) {
                        Kolor.TEXT("The server glitched and deposited ") + round(Math.random() * 10000) + Kolor.ACCENT(" $coin1") + " to your wallet."
                    }
                    return false
                }
                val coin2 = args.getOrNull(3)?.let { Market.getCoin(it) } ?: return false
                if (coin1 == coin2) {
                    Text(sender) {
                        Kolor.ERROR("FOOL! You can't swap for the same coin!")
                    }
                    return false
                }
                val balance = wallet.balance[coin2] ?: BigDecimal.ZERO
                val message = when (wallet.swap(coin1, coin2, amount)) {
                    BUYER_MISSING_COIN, INSUFFICIENT_BUYER_BALANCE -> Kolor.ERROR("Erm......Idk what to say but that just didn't work.")
                    INSUFFICIENT_SELLER_BALANCE, SELLER_MISSING_COIN -> Kolor.ERROR("You don't have enough ") + Kolor.ERROR.accent(
                        coin1.toString()
                    ) + " to swap!"
                    EVENT_CANCELLED -> return false
                    PRICE_UNAVAILABLE -> Kolor.ERROR("No price is set for this trade!")
                    else -> {
                        val swapped = wallet.balance[coin2]?.minus(balance) ?: BigDecimal.ZERO
                        Kolor.ALT("Swapped ") + amount.rounded + Kolor.ACCENT(" $coin1") + Kolor.TEXT(" for ") + swapped.rounded + Kolor.ACCENT(" $coin2") + "."
                    }
                }
                message.send(sender)
            }

            "balance", "bal" -> {
                val coin = args.getOrNull(1)?.let { Market.getCoin(it) }
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
                var coin = args.getOrNull(2)?.let { Market.getCoin(it) }
                if(coin == null && amountString == "all") {
                    var success = false
                    for ((key, coin) in Market.coins) {
                        if (coin.backing == null) continue
                        if(action == "withdraw" && ((wallet.balance[coin] ?: BigDecimal.ZERO) < BigDecimal.ONE)) continue
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
                                player.countIncludingBlocks(coin.backing, coin.backingBlock).toBigDecimal()
                            } else {
                                player.inventory.sumOf { it?.amount ?: 0 }.toBigDecimal()
                            }
                        } else null
                    } else {
                        wallet.balance[coin] ?: BigDecimal.ZERO
                    }
                } else {
                    amountString.toBigDecimalOrNull()
                }
                if (amount == null) {
                    Text(sender) {
                        Kolor.ERROR("Erm? How much $coin would you like to ${if (action == "deposit") "deposit" else "withdraw"}..?")
                    }
                    return false
                } else if (!amount.isMoreThanZero) {
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
                val message = when (result) {
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

                    EVENT_CANCELLED -> return false
                    SQL_ERROR -> Kolor.ERROR("Erm... Something blew up and it's all my fault!")
                    else -> {
                        Kolor.ALT("Nice $action! ") + Kolor.TEXT("You now have ") + (wallet.balance[coin]
                            ?: BigDecimal.ZERO).rounded + Kolor.ACCENT(" $coin") + "."
                    }
                }
                message.send(sender)
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
                val coin = args.getOrNull(3)?.let { Market.getCoin(it) } ?: Coin.DIA
                val amount = runCatching {
                    args[2].run {
                        if (this == "all") wallet.balance[coin]
                        else toBigDecimal()
                    }
                }.getOrNull()
                if (amount == null) {
                    Text(sender) {
                        Kolor.ERROR("Erm? How much ") + Kolor.ERROR.accent(coin.toString()) + Kolor.ERROR(" would you like to $action $actionText ") + Kolor.ERROR.alt(
                            args[1]
                        ) + "..?"
                    }
                    return false
                } else if (!amount.isMoreThanZero) {
                    Text(sender) {
                        Kolor.TEXT("The server glitched and deposited ") + round(Math.random() * 10000) + Kolor.ACCENT(" $coin") + " to your wallet."
                    }
                    return false
                } else if (!amount.isMoreThanZero) {
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
                    } else if ((targetWallet.balance[coin] ?: BigDecimal.ZERO) < amount) {
                        Text(sender) {
                            Kolor.ALT(args[1]) + Kolor.ERROR(" doesn't have enough ") + Kolor.ERROR.accent(coin.toString()) + " to spare!"
                        }
                        return false
                    }
                    PlayerRequestCoinEvent(player, targetWallet, amount, coin).fire() ?: return false //TODO: message?
                    Text(sender) {
                        Kolor.TEXT("Requested ") + amount.rounded + Kolor.ACCENT(" $coin") + " from " + Kolor.ALT(args[1]) + "."
                    }
                    Text(targetPlayer) {
                        Kolor.ALT("@${sender.name}") + Kolor.TEXT(" is requesting ") + amount.rounded + Kolor.ACCENT(" $coin") + Kolor.TEXT(" from you!\n") +
                                Kolor.ALT.accent("(Click here to send)")
                                    .suggest("/wallet send @${sender.name} $amount ${coin.ticker}")
                    }
                    return true
                }
                val message = when (wallet.send(targetWallet, coin, amount)) {
                    MISSING_COIN, INSUFFICIENT_BALANCE -> Kolor.ERROR("You do not even have any ") + Kolor.ERROR.accent(
                        coin.toString()
                    ) + Kolor.ERROR("!")

                    SQL_ERROR -> Kolor.ERROR("Sorry to break it to you but that just didn't work!")
                    EVENT_CANCELLED -> return false
                    else -> {
                        Text(targetPlayer) {
                            (Kolor.ALT("@${sender.name}") + Kolor.TEXT(" sent you ") + amount.rounded + Kolor.ACCENT(" $coin") + Kolor.TEXT("!")).execute("/wallet balance")
                        }
                        (Kolor.TEXT("Sent ") + amount.rounded + Kolor.ACCENT(" $coin") + " to " + Kolor.ALT(args[1]) + ".").execute("/wallet balance")
                    }
                }
                message.send(sender)
            }
        }
        return true
    }
}