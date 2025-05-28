package co.akoot.plugins.bluefox.api

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.extensions.invoke
import co.akoot.plugins.bluefox.extensions.isBedrock
import co.akoot.plugins.bluefox.util.Text
import net.kyori.adventure.text.Component
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.command.defaults.BukkitCommand
import org.bukkit.entity.Player

abstract class FoxCommand(val plugin: FoxPlugin, val id: String, description: String = "", defaultUsage: String = "/$id", vararg aliases: String) : BukkitCommand(id, description, defaultUsage, aliases.toList()) {

    companion object {
        private val SELECTORS = setOf("@a", "@s", "@r")
        private val OFFLINE_SELECTORS = setOf("@ao", "@ro")
        private val ALL_SELECTORS = SELECTORS + OFFLINE_SELECTORS
        val nothing: MutableList<String> = mutableListOf()
    }

    private val permissionNode = "${plugin.name}.command.$id"

    init {
        permission = permissionNode
    }

    /**
     * Filter the strings in the list to include only the ones the sender has permission to use.
     * This is typically used for generating sub-command tab-completion
     */
    protected fun MutableList<String>.permissionCheck(sender: CommandSender): MutableList<String> {
        return this.filter { permissionCheck(sender, it) == true }.toMutableList()
    }

    /**
     * Convert a list of arguments into one single argument separated by spaces
     *
     * @param args A list of arguments
     * @param index The index at which to start slicing [args] and join to a [String].
     *
     * @return A single argument separated by spaces. If [args] is null or [index] is out of bounds, returns null
     */
    fun varargs(args: Array<out String>?, index: Int = 0): String? {
        if (args == null || index >= args.size) return null
        return (if (index != 0) args.sliceArray(index..<args.size) else args).joinToString(" ")
    }

    /**
     * Get a list of suggestions which contain offline player names that are not in [args]
     *
     * @param args A list of player names already provided. This will filter out these names from the suggestions.
     * If the last argument starts with "." suggest bedrock names.
     *
     * @return A list of suggestions which contain offline player names
     */
    fun getOfflinePlayerSuggestions(args: Array<out String>? = null, exclude: Set<String> = setOf(), prefix: String = ""): MutableList<String> {
        val offlinePlayerNames = BlueFox.cachedOfflinePlayerNames.map { "$prefix$it" }.minus(exclude).toMutableList()
        return getPlayerSuggestions(offlinePlayerNames, args, prefix)
    }

    fun getOnlinePlayerSuggestions(args: Array<out String>? = null, exclude: Set<String> = setOf(), prefix: String = ""): MutableList<String> {
        val onlinePlayerNames = plugin.server.onlinePlayers.mapNotNull { "$prefix${it.name}" }.minus(exclude).toMutableList()
        return getPlayerSuggestions(onlinePlayerNames, args, prefix)
    }

    fun getPlayerSuggestions(players: MutableList<String>, args: Array<out String>? = null, prefix: String = ""): MutableList<String> {
        if (args?.last()?.startsWith("$prefix.") == false) players.removeIf {it.startsWith("$prefix.")}
        if (args.isNullOrEmpty()) return players
        return players.filterNot { it in args }.toMutableList()
    }

    /**
     * Perform a check on the [sender] and returns null if they are not of type [Player]. This is used to quickly exit
     * [onCommand] if the [sender] is not a player
     *
     * @param sender The command sender
     * @param message The "no permission" message to send to the [sender]. By default, it is
     * "You need to be a player to run /[id]". If null, no message will be sent
     *
     * @return The [sender] cast to [Player] if the sender is a player, null otherwise
     */
    fun playerCheck(sender: CommandSender, message: Text? = Kolor.ERROR("You need to be a player in order to run ") + Kolor.ERROR.accent(
        "/$id"
    )): Player? {
        if (sender !is Player) {
            message?.send(sender)
            return null
        }
        return sender
    }

    /**
     * Perform a permission check on the [sender] and returns null if they do not. This is used to quickly exit
     * [onCommand] if the [sender] doesn't have permission
     *
     * @param sender The command sender
     * @param node The permission node (if applicable) as "$permissionNode.$node"
     * @param message The "no permission" message to send to the [sender]. By default, it is
     * "You do not have permission to use /[id]". If null, no message will be sent
     *
     * @return True if the sender has permission, null otherwise
     */
    fun permissionCheck(sender: CommandSender, node: String? = null, message: Text? = Kolor.ERROR("You do not have permission to use ") + Kolor.ERROR.accent(
        "/$id"
    )): Boolean? {
        if (!hasPermission(sender, node)) {
            message?.send(sender)
            return null
        }
        return true
    }

    /**
     * Get whether a CommandSender has the permission node "[plugin].command.[id].[node]"
     *
     * @param sender The command sender
     * @param node The permission node that goes after [plugin].command.[id] If null, it will just check [plugin].command.[id]
     *
     * @return Whether the [sender] has the permission node
     */
    fun hasPermission(sender: CommandSender, node: String? = null): Boolean {
        node ?: return sender.hasPermission(permissionNode)
        return sender.hasPermission("$permissionNode.$node")
    }

    /**
     * Computes the tab completion for the command. If the last argument is not blank, only send the suggestions
     * that contain the last argument (case-insensitive)
     *
     * @param sender The command sender
     * @param cmd The command/alias used
     * @param args The arguments that the [sender] has typed thus far
     *
     * @return The list of tab-completion suggestions that only send the suggestions that contain the last argument
     * (case-insensitive)
     */
    override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>): List<String?> {

        // Suggestions variable
        val suggestions = onTabComplete(sender, alias, args)

        // If the last argument is empty, return all suggestions
        if(args.last().isEmpty()) return suggestions

        // Return all the suggestions that contain the last argument
        return suggestions.filter { it.contains(args.last(), true) }.toMutableList()
    }

    /**
     * Executes the command
     *
     * @param sender The command sender
     * @param cmd The command/alias used
     * @param args The arguments that the [sender] has provided
     *
     * @return Whether the execution was a success, I guess
     */
    override fun execute(sender: CommandSender, cmd: String, args: Array<out String>): Boolean {
        return onCommand(sender, cmd, args)
    }

    /**
     * A list of tab-completion suggestions
     *
     * @param sender The command sender
     * @param args The arguments that the [sender] has typed thus far
     *
     * @return A list of tab-completion suggestions
     */
    abstract fun onTabComplete(sender: CommandSender, alias: String, args: Array<out String>): MutableList<String>

    /**
     * Execute the command
     *
     * @param sender The command sender
     * @param args The list of arguments that the [sender] has provided
     *
     * @return Whether the execution was a success, I guess
     */
    abstract fun onCommand(sender: CommandSender, alias: String, args: Array<out String>): Boolean

    open fun sendUsage(sender: CommandSender): Boolean {
        sender.sendMessage("Usage:")
        //TODO idk
        return false
    }

    fun getPlayer(name: String): Result<Player?> {
        val player = BlueFox.getPlayer(name)
            ?: return Result.fail(null, Kolor.ERROR("Player ") + Kolor.ERROR.accent(name) + Kolor.ERROR(" is not online!"))
        return Result(player)
    }

    fun getOfflinePlayer(name: String): Result<OfflinePlayer?> {
        val player = BlueFox.getOfflinePlayer(name)
            ?: return Result.fail(null, Kolor.ERROR("Player ") + Kolor.ERROR.accent(name) + Kolor.ERROR(" does not exist!"))
        return Result(player)
    }

    fun getPlayerSender(sender: CommandSender): Result<Player?> {
        if (sender !is Player) {
            return Result.fail(null, "You must be a player to use this command!")
        }
        return Result(sender)
    }

    fun sendError(sender: CommandSender, message: String, rawColor: Boolean = false): Boolean {
        Kolor.ERROR(message, sender.isBedrock, rawColor).send(sender)
        return false
    }

    fun sendMessage(sender: CommandSender, message: String, rawColor: Boolean = false): Boolean {
        Kolor.TEXT(message, sender.isBedrock, rawColor).send(sender)
        return true
    }

    class Result<T>(val value: T, private val message: Text? = null) {

        constructor(value: T, message: Component): this(value, Text(message))
        constructor(value: T, message: String): this(value, Text(message))

        companion object {

            val FAIL = Result(false)
            val SUCCESS = Result(true)

            fun<T> fail(value: T, message: Component): Result<T> {
                return Result(value, message)
            }

            fun<T> fail(value: T, message: Text): Result<T> {
                return Result(value, message)
            }

            fun<T> fail(value: T, message: String): Result<T> {
                return Result(value, Kolor.ERROR(message))
            }

            fun fail(message: Component): Result<Boolean> {
                return Result(false, message)
            }

            fun fail(message: String): Result<Boolean> {
                return Result(false, Kolor.ERROR(message))
            }

            fun fail(message: Text): Result<Boolean> {
                return Result(false, message.component.colorIfAbsent(Kolor.ERROR.get(message.bedrock, message.rawColor)))
            }

            fun<T> success(value: T, component: Component): Result<T> {
                return Result(value, component)
            }

            fun<T> success(value: T, message: Text): Result<T> {
                return Result(value, message)
            }

            fun<T> success(value: T, message: String): Result<T> {
                return Result(value, Kolor.TEXT(message))
            }

            fun success(message: Component): Result<Boolean> {
                return Result(true, message)
            }

            fun success(message: Text): Result<Boolean> {
                return Result(true, message.component.colorIfAbsent(Kolor.TEXT.get(message.bedrock, message.rawColor)))
            }

            fun success(message: String): Result<Boolean> {
                return Result(true, Kolor.TEXT(message))
            }
        }

        fun send(sender: CommandSender): Result<T> {
            message?.send(sender)
            return this
        }

        fun getAndSend(sender: CommandSender): T {
            message?.send(sender)
            return value
        }

    }
}