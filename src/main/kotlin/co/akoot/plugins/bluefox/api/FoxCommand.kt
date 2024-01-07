package co.akoot.plugins.bluefox.api

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

abstract class FoxCommand : TabExecutor {

    lateinit var sender: CommandSender

    /**
     * Returns the sender as a user if they are one, null otherwise
     */
    val userSender: User? get() = if (sender !is Player) null else User(sender as Player)


    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        cmd: String,
        args: Array<out String>?
    ): MutableList<String>? {
        this.sender = sender
        return onTabComplete()
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        cmd: String,
        args: Array<out String>?
    ): Boolean {
        this.sender = sender
        return onCommand()
    }

    /**
     * Execute the command
     */
    abstract fun onCommand(): Boolean

    /**
     * Tab complete
     */
    abstract fun onTabComplete(): MutableList<String>?
}