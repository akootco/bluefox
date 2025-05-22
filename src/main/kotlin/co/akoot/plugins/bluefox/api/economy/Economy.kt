package co.akoot.plugins.bluefox.api.economy

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.Kolor
import co.akoot.plugins.bluefox.extensions.invoke
import co.akoot.plugins.bluefox.util.Text
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.command.CommandSender

object Economy {

    object Success {
        const val SUCCESS = 0
    }

    object Error {
        const val INSUFFICIENT_BALANCE = -1
        const val INSUFFICIENT_SELLER_BALANCE = -2
        const val INSUFFICIENT_BUYER_BALANCE = -3
        const val MISSING_COIN = -4
        const val SELLER_MISSING_COIN = -5
        const val BUYER_MISSING_COIN = -6
        const val SQL_ERROR = -7
        const val INSUFFICIENT_ITEMS = -8
        const val COIN_HAS_NO_BACKING = -9
        const val NUMBER_TOO_SMALL = -10
        const val PRICE_UNAVAILABLE = -11
        const val INVALID_GAME_MODE = -12
        const val INVALID_WORLD = -13
    }

    val Double.rounded: String get() = "%.9f".format(this)

    fun sendWallet(sender: CommandSender, wallet: Wallet, self: Boolean = true) {
        val coins = wallet.balance.keys
        if (coins.isEmpty() || wallet.balance.values.sum() <= 0.0) {
            if(self) {
                Text(sender) {
                    Kolor.WARNING("You are broke! Deposit some items for some coins!")
                }
                Text(sender) {
                    Kolor.WARNING.quote("/wallet deposit <amount> [coin]").italic().suggest("/wallet deposit ")
                }
                for (coin in Market.coins.values) {
                    if (coin.backing == null) continue
                    Text(sender) {
                        Text() + Component.translatable(coin.backing.translationKey())
                            .color(Kolor.WARNING.alt) + Kolor.WARNING(" -> ") + Kolor.WARNING.accent("$coin")
                    }
                }
            } else {
                Text(sender) {
                    Kolor.WARNING("They are broke!!!")
                }
            }
            return
        }
        Text(sender) {
            Kolor.TEXT.alt("[") + Kolor.ALT("0x${wallet.address}") + Kolor.TEXT.alt("]")
        }
        for (coin in coins) {
            sendBalance(sender, wallet, coin)
        }
    }

    fun sendBalance(sender: CommandSender, wallet: Wallet, coin: Coin, prefix: String = "") {
        Text(sender) {
            Kolor.TEXT(prefix) + Kolor.ACCENT("$coin: ") + (wallet.balance[coin] ?: 0.0).rounded
        }
    }

    fun sendInfo(sender: CommandSender, coin: Coin) {
        val backing = coin.backing?.let {Component.translatable(it.translationKey()).color(Kolor.ERROR.accent)}
        Text(sender) {
            Kolor.ACCENT(coin.toString()) + " (" + Kolor.ALT(coin.name) + ") - " + Kolor.QUOTE(coin.description)
        }
        if (backing == null) return
        Text(sender) {
            Kolor.TEXT("  Backing item: ") + backing
        }
    }

    fun sendPrice(sender: CommandSender, coin1: Coin, coin2: Coin = Coin.DIA) {
        val price = Market.prices[coin2 to coin1]

        Text(sender) {
            Kolor.ACCENT(coin1.toString()) + Kolor.TEXT(" is worth ") + Kolor.NUMBER(price?.rounded ?: "(unknown)") + " " + Kolor.ACCENT(coin2.toString())
        }
    }
}