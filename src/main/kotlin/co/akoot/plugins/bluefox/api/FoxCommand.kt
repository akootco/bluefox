package co.akoot.plugins.bluefox.api

import co.akoot.plugins.bluefox.BlueFox
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.command.defaults.BukkitCommand

abstract class FoxCommand(val name: String, vararg val aliases: String): BukkitCommand(name) {

    override fun tabComplete(
        sender: CommandSender, cmd: String, args: Array<out String>?
    ): MutableList<String> {
        return onTabComplete(sender, args)
    }

    override fun execute(sender: CommandSender, cmd: String, args: Array<out String>?): Boolean {
        return onCommand(sender, args)
    }

    abstract fun onTabComplete(sender: CommandSender, args: Array<out String>?): MutableList<String>
    abstract fun onCommand(sender: CommandSender, args: Array<out String>?): Boolean
}