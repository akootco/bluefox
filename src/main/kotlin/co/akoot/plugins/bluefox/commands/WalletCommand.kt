package co.akoot.plugins.bluefox.commands

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.FoxCommand
import co.akoot.plugins.bluefox.api.economy.Wallet
import org.bukkit.command.CommandSender

class WalletCommand(plugin: BlueFox): FoxCommand(plugin,"wallet") {

    override fun onTabComplete(
        sender: CommandSender,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        return mutableListOf()
    }

    override fun onCommand(
        sender: CommandSender,
        alias: String,
        args: Array<out String>
    ): Boolean {
        sender.sendMessage("erm erm erm")
        val player = getPlayerSender(sender).getAndSend(sender) ?: return false
        val wallet = Wallet.get(player) ?: return false
        sender.sendMessage("your address is ${wallet.address} with id=${wallet.id}")
        return true
    }
}