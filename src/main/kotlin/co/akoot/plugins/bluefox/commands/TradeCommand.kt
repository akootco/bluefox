package co.akoot.plugins.bluefox.commands

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.FoxCommand
import co.akoot.plugins.bluefox.api.Kolor
import co.akoot.plugins.bluefox.api.economy.Coin
import co.akoot.plugins.bluefox.api.economy.Economy.Error.INSUFFICIENT_BUYER_BALANCE
import co.akoot.plugins.bluefox.api.economy.Economy.Error.INSUFFICIENT_SELLER_BALANCE
import co.akoot.plugins.bluefox.api.economy.Economy.Error.BUYER_MISSING_COIN
import co.akoot.plugins.bluefox.api.economy.Economy.Error.SELLER_MISSING_COIN
import co.akoot.plugins.bluefox.api.economy.Economy.isMoreThanZero
import co.akoot.plugins.bluefox.api.economy.Economy.rounded
import co.akoot.plugins.bluefox.api.economy.Market
import co.akoot.plugins.bluefox.api.economy.Wallet
import co.akoot.plugins.bluefox.extensions.invoke
import co.akoot.plugins.bluefox.extensions.wallet
import co.akoot.plugins.bluefox.util.Text
import co.akoot.plugins.bluefox.util.Text.Companion.plus
import co.akoot.plugins.bluefox.util.Text.Companion.text
import org.bukkit.command.CommandSender
import java.math.BigDecimal

class TradeCommand(plugin: BlueFox): FoxCommand(plugin, "trade") {


    override fun onTabComplete(
        sender: CommandSender,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        return when(args.size) {
            1 -> getOnlinePlayerSuggestions(exclude = setOf("@" + sender.name), prefix = "@")
            2,5 -> mutableListOf("1")
            3 -> Market.coins.keys.toMutableList()
            4 -> mutableListOf("for")
            6 -> Market.coins.keys.minus(args[2].uppercase()).toMutableList()
            else -> mutableListOf()
        }
    }

    override fun onCommand(
        sender: CommandSender,
        alias: String,
        args: Array<out String>
    ): Boolean {
        val player = getPlayerSender(sender).getAndSend(sender) ?: return false
        val targetPlayer = getPlayer(args[0].substring(1)).getAndSend(sender) ?: return false
        val wallet = player.wallet
        val targetWallet = targetPlayer.wallet
        val amount1 = runCatching { args[1].toBigDecimal() }.getOrNull()
        val amount2 = runCatching { args[4].toBigDecimal() }.getOrNull()
        val coin1 = args.getOrNull(2)?.let { Market.getCoin(it) }
        val coin2 = args.getOrNull(5)?.let { Market.getCoin(it) }
        if (wallet == null || targetWallet == null) {
            Text(sender) {
                Kolor.ERROR("Both players must have a wallet to trade! Erm!!!!")
            }
            return false
        }
        if (amount1 == null || amount2 == null) {
            Text(sender) {
                Kolor.ERROR("Try putting in a number for the amount..?")
            }
            return false
        } else if(!amount1.isMoreThanZero || !amount2.isMoreThanZero) {
            Text(sender) {
                Kolor.ERROR("Now that wouldn't be very fair!")
            }
            return false
        }
        if (coin1 == null || coin2 == null) {
            Text(sender) {
                Kolor.ERROR("Try using valid coin tickers..?")
            }
            return false
        }
        if (coin1 == coin2) {
            Text(sender) {
                Kolor.ERROR("You can't trade for the same coin!")
            }
            return false
        }
        val price1 = coin1 to amount1
        val price2 = coin2 to amount2
        val key = Market.getTradeKey(wallet to targetWallet)
        if(key?.second == wallet) {
            val result = Market.trade(wallet, targetWallet, coin1, coin2, amount1, amount2)
            if(result < 0) {
                Text(sender) {
                    when(result) {
                        SELLER_MISSING_COIN, INSUFFICIENT_SELLER_BALANCE -> Kolor.ERROR("You don't have enough ") + Kolor.ERROR.accent(coin1.toString()) + "!!"
                        BUYER_MISSING_COIN, INSUFFICIENT_BUYER_BALANCE -> Kolor.ERROR.alt(args[1]) + Kolor.ERROR(" doesn't have enough ") + Kolor.ERROR.accent(coin1.toString()) + "!!"
                        else -> Kolor.ERROR("Something went wrong ($result).............. Please try again later..................")
                    }
                }
                return false
            }
            Text(sender) {
                (Kolor.TEXT("Accepted ") + Kolor.ALT(args[0]) + Kolor.TEXT("'s trade offer: ") + amount2.rounded + Kolor.ACCENT(" $coin2") + Kolor.TEXT(" for ") + amount1.rounded + Kolor.ACCENT(" $coin1")).execute("/wallet balance")
            }
            Text(targetPlayer) {
                (Kolor.ALT("@${sender.name}") + Kolor.TEXT(" accepted your trade offer: ") + amount2.rounded + Kolor.ACCENT(" $coin2")+ Kolor.TEXT(" for ") + amount1.rounded + Kolor.ACCENT(" $coin1")).execute("/wallet balance")
            }
        } else {
            Market.requestTrade(wallet to targetWallet, price1, price2)
            Text(sender) {
                Kolor.TEXT("Sent ") + Kolor.ALT(args[0]) + Kolor.TEXT(" a trade offer: ") + amount1.rounded + Kolor.ACCENT(" $coin1") + Kolor.TEXT(" for ") + amount2.rounded + Kolor.ACCENT(" $coin2") + "."
            }
            Text(targetPlayer) {
                Kolor.ALT("@${sender.name}") + Kolor.TEXT(" sent a trade offer: ") + amount1.rounded + Kolor.ACCENT(" $coin1")+ Kolor.TEXT(" for ") + amount2.rounded + Kolor.ACCENT(" $coin2") + ".\n" +  Kolor.ACCENT.alt("(Click here to accept)").suggest("/trade @${player.name} $amount2 ${coin2.ticker} for $amount1 ${coin1.ticker}")
            }
        }
        return true
    }


}