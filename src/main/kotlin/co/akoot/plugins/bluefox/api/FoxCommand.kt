package co.akoot.plugins.bluefox.api

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.util.Text
import co.akoot.plugins.bluefox.util.Text.Companion.plus
import co.akoot.plugins.bluefox.util.Text.Companion.plusAssign
import net.kyori.adventure.text.Component
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.command.defaults.BukkitCommand
import org.bukkit.entity.Player
import net.kyori.adventure.text.TextComponent

abstract class FoxCommand(val plugin: FoxPlugin, val id: String, description: String = "", defaultUsage: String = "/$id", vararg aliases: String) : BukkitCommand(id, description, defaultUsage, aliases.toList()) {

    companion object {
        private const val PLACEHOLDER_CHARS = "$@^#"
        private val PLACEHOLDER_REGEX = Regex("([$PLACEHOLDER_CHARS])\\{?(\\w+)}?")
        private val SELECTORS = setOf("@a", "@s", "@r")
        private val OFFLINE_SELECTORS = setOf("@ao", "@ro")
        private val ALL_SELECTORS = SELECTORS + OFFLINE_SELECTORS

        /**
         * Computes a [Component] using the provided [message] and [placeholders].
         *
         * @param message The message to send which includes [placeholders]. If there is a string that starts with
         * a placeholder symbol, it will be replaced with the placeholder. For example "Your health is $health"
         * @param placeholders The placeholders. A [Pair] of type [String] and [Any] where A maps to B, for example
         * Pair("$health", player.getHealth())
         * @param error Whether to use error colors or not
         *
         * @return The computed [Component]
         */
        fun getMessage(
            message: String,
            vararg placeholders: Pair<String, Any?>,
            error: Boolean = false,
            bedrock: Boolean = false
        ): Component {
            // Add the "error_" prefix if it's an error message
            val errorPrefix = if (error) "error_" else ""

            // Skip the whole placeholders parsing if the message doesn't contain anything
            if (placeholders.isEmpty()) return Text(message, "${errorPrefix}text").component

            // Final component
            val component = Component.text()

            // Convert the list of placeholder Pairs to a Map
            val map = placeholders.toMap()

            // ...Variables
            val parts = PLACEHOLDER_REGEX.split(message).filterNot { it.isBlank() }
            val matches = PLACEHOLDER_REGEX.findAll(message)

            val startsWithPlaceholder = message[0] in PLACEHOLDER_CHARS

            // Iterate through the matches
            for ((i, match) in matches.withIndex()) {

                // Get the key and value from the matches and the placeholders
                val key = match.groupValues[2]
                val value = map[key]


                // If the placeholder value is a Component, just skip the rest and add that to the result
                if (value is Component) {
                    component += Text(parts[i], "text", bedrock)
                    component += value
                    continue
                }

                // Get the color from the code
                val code = when (match.groupValues[1]) {
                    "$" -> "accent"
                    "#" -> "number"
                    "@" -> "player"
                    else -> "text"
                }

                // Helper variables to help with scenarios that the message starts with a placeholder character
                val startWithPlaceholderMode = i == 0 && startsWithPlaceholder
                val partComponent = Text(parts[i], "text")

                // If the message did nit start with a placeholder prefix, append the part now
                if(!startWithPlaceholderMode) component += partComponent

                // Append the value with the color
                component += Text(value.toString(), "${errorPrefix}${code}")

                // If the message started with a placeholder prefix, append the part later
                if(startWithPlaceholderMode) component += partComponent
            }

            // Append any remaining part of the message after the last placeholder
            if (matches.count() < parts.size) {
                component += Text(parts.last(), "text")
            }

            // Return the component
            return component.build()
        }
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
     * If the last argument starts with . suggest bedrock names.
     *
     * @return A list of suggestions which contain offline player names
     */
    fun getOfflinePlayerSuggestions(args: Array<out String>? = null, exclude: Set<String> = setOf()): MutableList<String> {
        val offlinePlayerNames = plugin.server.offlinePlayers.mapNotNull { it.name }.minus(exclude).toMutableList()
        return getPlayerSuggestions(offlinePlayerNames, args)
    }

    fun getOnlinePlayerSuggestions(args: Array<out String>? = null, exclude: Set<String> = setOf()): MutableList<String> {
        val onlinePlayerNames = plugin.server.onlinePlayers.mapNotNull { it.name }.minus(exclude).toMutableList()
        return getPlayerSuggestions(onlinePlayerNames, args)
    }

    fun getPlayerSuggestions(players: MutableList<String>, args: Array<out String>? = null): MutableList<String> {
        if (args?.last()?.startsWith(".") == false) players.removeIf {it.startsWith(".")}
        if (args.isNullOrEmpty()) return players
        return players.filterNot { it in args }.toMutableList()
    }

    /**
     * Perform a check on the [sender] and returns null if they are not of type [Player]. This is used to quickly exit
     * [onCommand] if the [sender] is not a player
     *
     * @param sender The command sender
     * @param message The "no permission" message to send to the [sender]. By default, it is
     * "You need to be a player in order to run /[id]". If null, no message will be sent
     * @param placeholders The placeholders, if any, to replace in the [message]
     *
     * @return The [sender] cast to [Player] if the sender is a player, null otherwise
     */
    fun playerCheck(sender: CommandSender, message: String? = "You need to be a player in order to run /$id.", vararg placeholders: Pair<String, Any>): Player? {
        if (sender !is Player) {
            if(message != null) sendError(sender, message, *placeholders)
            return null
        }
        return sender
    }

    /**
     * Perform a permission check on the [sender] and returns null if they do not. This is used to quickly exit
     * [onCommand] if the [sender] doesn't have permission
     *
     * @param sender The command sender
     * @param node The permission node (if applicable)
     * @param message The "no permission" message to send to the [sender]. By default, it is
     * "You do not have permission to use /[id]". If null, no message will be sent
     * @param placeholders The placeholders, if any, to replace in the [message]
     *
     * @return True if the sender has permission, null otherwise
     */
    fun permissionCheck(sender: CommandSender, node: String? = null, message: String? = "You do not have permission to use /$id.", vararg placeholders: Pair<String, Any>): Boolean? {
        if (!hasPermission(sender, node)) {
            if(message != null) sendError(sender, message, *placeholders)
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
        val suggestions = onTabComplete(sender, args)

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
    abstract fun onTabComplete(sender: CommandSender, args: Array<out String>): MutableList<String>

    /**
     * Execute the command
     *
     * @param sender The command sender
     * @param args The list of arguments that the [sender] has provided
     *
     * @return Whether the execution was a success, I guess
     */
    abstract fun onCommand(sender: CommandSender, alias: String, args: Array<out String>): Boolean

    /**
     * Send a message to the [sender]
     *
     * @param sender The command sender
     * @param message The message to send which includes [placeholders]
     * @param placeholders The placeholders
     * @param error Whether to use error colors or not
     */
    fun sendMessage(sender: CommandSender, message: String, vararg placeholders: Pair<String, Any?>, error: Boolean = false): Boolean {
        sender.sendMessage(getMessage(message, *placeholders, error = error))
        return true
    }

    /**
     * Send an error message to a command sender
     *
     * @param sender The command sender
     * @param message The message to send which includes [placeholders]. If there is a string that starts with
     * a placeholder symbol, it will be replaced with the placeholder. For example "Your health is $health"
     * @param placeholders The placeholders. A [Pair] of type [String] and [Any] where A maps to B, for example
     * Pair("$health", player.getHealth())
     */
    fun sendError(sender: CommandSender, message: String, vararg placeholders: Pair<String, Any>): Boolean {
        sendMessage(sender, message, *placeholders, error = true)
        return false
    }

    /**
     *
     * Computes a [Component] using the provided [message] and [placeholders].
     *
     * @param message The message to send which includes [placeholders]. If there is a string that starts with
     * a placeholder symbol, it will be replaced with the placeholder. For example "Your health is $health"
     * @param placeholders The placeholders. A [Pair] of type [String] and [Any] where A maps to B, for example
     * Pair("$health", player.getHealth())
     *
     * @return The computed [Component]
     */
    fun getErrorMessage(message: String, vararg placeholders: Pair<String, Any>): Component {
        return getMessage(message, *placeholders, error = true)
    }

    open fun sendUsage(sender: CommandSender, vararg usages: String = arrayOf("/$name")): Boolean {
        if (usage.length == 1) {
            sendMessage(sender, "Usage: \$USAGE", "USAGE" to usage)
            return false
        }
        sendMessage(sender, "Usage:")
        for (usage in usages) {
            sendMessage(sender, "\t$usage")
        }
        return false
    }

    fun getPlayer(name: String): Result<Player?> {
        val player = BlueFox.getPlayer(name)
        if (player == null) {
            return Result.fail(null, "Player \$PLAYER not online!", "PLAYER" to name)
        }
        return Result(player)
    }

    fun getOfflinePlayer(name: String): Result<OfflinePlayer?> {
        val player = BlueFox.getOfflinePlayer(name)
        if (player == null) {
            return Result.fail(null, "Player \$PLAYER does not exist!", "PLAYER" to name)
        }
        return Result(player)
    }

    fun getPlayerSender(sender: CommandSender): Result<Player?> {
        if (sender !is Player) {
            return Result.fail(null, "You must be a player to use this command!")
        }
        return Result(sender)
    }

    class Result<T>(val value: T, val message: Component? = null) {

        constructor(value: T, message: String, vararg placeholders: Pair<String, Any>):
            this(value, getMessage(message, *placeholders, error=(value == null || value == false)))

        companion object {

            val FAIL = Result(false)
            val SUCCESS = Result(true)

            fun<T> fail(value: T, message: String, vararg placeholders: Pair<String, Any>): Result<T> {
                return Result(value, message, *placeholders)
            }

            fun<T> fail(value: T, message: Component): Result<T> {
                return Result(value, message)
            }

            fun fail(message: String, vararg placeholders: Pair<String, Any>): Result<Boolean> {
                return Result(false, message, *placeholders)
            }

            fun fail(message: Component): Result<Boolean> {
                return Result(false, message)
            }

            fun success(message: String, vararg placeholders: Pair<String, Any>): Result<Boolean> {
                return Result(true, message, *placeholders)
            }

            fun success(message: Component): Result<Boolean> {
                return Result(true, message)
            }
        }

        fun send(sender: CommandSender): Result<T> {
            message?.let { sender.sendMessage(it) }
            return this
        }

    }
}