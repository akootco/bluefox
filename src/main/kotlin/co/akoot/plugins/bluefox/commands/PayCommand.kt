package co.akoot.plugins.bluefox.commands

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.CatCommand
import co.akoot.plugins.bluefox.api.economy.Economy
import co.akoot.plugins.bluefox.api.economy.Economy.bd
import co.akoot.plugins.bluefox.api.economy.Market
import co.akoot.plugins.bluefox.api.economy.Wallet
import co.akoot.plugins.bluefox.extensions.playSound
import co.akoot.plugins.bluefox.extensions.wallet
import co.akoot.plugins.bluefox.util.accent
import co.akoot.plugins.bluefox.util.asCurrency
import co.akoot.plugins.bluefox.util.sendText
import co.akoot.plugins.bluefox.util.sendWarning
import co.akoot.plugins.bluefox.util.tertiary
import org.bukkit.OfflinePlayer
import org.bukkit.Sound
import org.bukkit.command.CommandSender

class PayCommand(plugin: BlueFox): CatCommand(plugin, "pay", "Pay players from anywhere!") {
    init {
        noargs {
            permissionCheck(it) ?: return@noargs false
        }
        then {
            offlinePlayer {
                permissionCheck(it) ?: return@offlinePlayer false
            } then {
                double(min = 0.0, max = Double.MAX_VALUE) {
                    permissionCheck(it) ?: return@double false
                    val sender = getSender(it)
                    val amount = getDouble(it)
                    val player = getOfflinePlayer(it)
                    pay(sender, player, amount)
                } then {
                    string("currency", { _, builder ->
                        for((key, coin) in Market.coins) {
                            if(key.startsWith(builder.remaining, true)) {
                                builder.suggest(key)
                            }
                        }
                    }) {
                        permissionCheck(it) ?: return@string false
                        val sender = getSender(it)
                        val currency = getString(it, "currency")
                        val amount = getDouble(it)
                        val player = getOfflinePlayer(it)
                        pay(sender, player, amount, currency)
                    }
                }
            }
        }
    }

    fun pay(sender: CommandSender, player: OfflinePlayer?, amount: Double, currency: String = "AKC"): Boolean {
        val player = player ?: return sender.sendWarning("Who even is bro? You typed their name wrong probably!")
        val wallet = player.wallet ?: return sender.sendWarning("It appears ", player, " does not have a wallet. Sucks!")
        val coin = Market.getCoin(currency) ?: return sender.sendWarning("There isn't a coin with the name \"", accent(currency), "\".")
        val amount = amount.bd
        Wallet.BANK.send(wallet, coin, amount)
        player.player?.apply {
            sendText("You have received ", tertiary(amount.asCurrency), " ", accent(currency.uppercase()), "!")
            playSound(Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
        }
        return sender.sendText("Paid ", player, " ", tertiary(amount.asCurrency), " ", accent(currency.uppercase()), "!")
    }
}