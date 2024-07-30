package co.akoot.plugins.bluefox.api

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.util.ColorUtil
import co.akoot.plugins.bluefox.util.Txt
import net.kyori.adventure.text.Component
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.command.defaults.BukkitCommand
import org.bukkit.entity.Player
import org.checkerframework.checker.regex.qual.Regex

abstract class FoxCommand(val plugin: FoxPlugin, val id: String, vararg val aliases: String): BukkitCommand(id) {

    val REGEX = Regex("([$@^#])(\\w+)")

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

    fun sendMessage(sender: CommandSender, message: String, vararg args: Pair<String, Any>, error: Boolean = false) {
        sender.sendMessage(getMessage(sender, message, *args, error = error))
    }

    fun getMessage(sender: CommandSender, message: String, vararg args: Pair<String, Any>, error: Boolean = false): Component {
        if (sender !is Player) return Component.text(message)

        val errorPrefix = if (error) "error_" else ""
        if (args.isEmpty()) return Txt(message, "${errorPrefix}text").c

        val map = args.toMap()
        val parts = REGEX.split(message)
        val matches = REGEX.findAll(message)
        val index = 0
        val result = Component.text()
        for (match in matches) {
            result.append(Txt(parts[index], "text").c)
            val key = match.groupValues[2]
            val value = map[key]
            if (value is Component) {
                result.append(value)
                continue
            }
            val code = when(match.groupValues[1]) {
                "$" -> "accent"
                "#" -> "number"
                "^" -> "text"
                "@" -> "player"
                else -> null
            }
            result.append(Txt(value.toString(), "${errorPrefix}${code}").c)
        }
        return result.build()
    }

    fun getErrorMessage(sender: CommandSender, message: String, vararg args: Pair<String, Any>): Component {
        return getMessage(sender, message, *args, error=true)
    }
}