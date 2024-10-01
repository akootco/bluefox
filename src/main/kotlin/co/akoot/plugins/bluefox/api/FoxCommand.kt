package co.akoot.plugins.bluefox.api

import co.akoot.plugins.bluefox.util.Txt
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.command.defaults.BukkitCommand
import org.bukkit.entity.Player

abstract class FoxCommand(val plugin: FoxPlugin, val id: String, vararg val aliases: String) : BukkitCommand(id) {

    companion object {
        private val PLACEHOLDER_CHARS = "$@^#"
        private val PLACEHOLDER_REGEX = Regex("([$PLACEHOLDER_CHARS])\\{?(\\w+)}?")
        val SELECTORS = setOf("@a", "@s", "@r")
        val OFFLINE_SELECTORS = setOf("@ao", "@ro")
        val ALL_SELECTORS = SELECTORS + OFFLINE_SELECTORS
    }

    private val permissionNode = "${plugin.name}.command.$id"

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
    fun getOfflinePlayerSuggestions(args: Array<out String>? = null): MutableList<String> {
        val offlinePlayerNames = plugin.server.offlinePlayers.mapNotNull { it.name }.toMutableList()
        if (args?.last()?.startsWith(".") == false) offlinePlayerNames.removeIf {it.startsWith(".")}
        if (args.isNullOrEmpty()) return offlinePlayerNames
        return offlinePlayerNames.filterNot { it in args }.toMutableList()
    }

    /**
     * Perform a check on the [sender] and returns null if they are not of type [org.bukkit.entity.Player]. This is used to quickly exit
     * [onCommand] if the [sender] is not a player
     *
     * @param sender The command sender
     * @param message The "no permission" message to send to the [sender]. By default, it is
     * "You need to be a player in order to run /[id]". If null, no message will be sent
     * @param placeholders The placeholders, if any, to replace in the [message]
     *
     * @return The [sender] cast to [org.bukkit.entity.Player] if the sender is a player, null otherwise
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
        node ?: sender.hasPermission(permissionNode)
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
    override fun tabComplete(
        sender: CommandSender, cmd: String, args: Array<out String>?
    ): MutableList<String> {
        // If args is null return an empty list
        args ?: return mutableListOf()

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
    override fun execute(sender: CommandSender, cmd: String, args: Array<out String>?): Boolean {
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
    abstract fun onTabComplete(sender: CommandSender, args: Array<out String>?): MutableList<String>

    /**
     * Execute the command
     *
     * @param sender The command sender
     * @param args The list of arguments that the [sender] has provided
     *
     * @return Whether the execution was a success, I guess
     */
    abstract fun onCommand(sender: CommandSender, alias: String, args: Array<out String>?): Boolean

    /**
     * Send a message to the [sender]
     *
     * @param sender The command sender
     * @param message The message to send which includes [placeholders]
     * @param placeholders The placeholders
     * @param error Whether to use error colors or not
     */
    fun sendMessage(sender: CommandSender, message: String, vararg placeholders: Pair<String, Any>, error: Boolean = false) {
        sender.sendMessage(getMessage(sender, message, *placeholders, error = error))
    }

    /**
     * Computes a [net.kyori.adventure.text.Component] using the provided [message] and [placeholders].
     *
     * @param sender The command sender
     * @param message The message to send which includes [placeholders]. If there is a string that starts with
     * a placeholder symbol, it will be replaced with the placeholder. For example "Your health is $health"
     * @param placeholders The placeholders. A [Pair] of type [String] and [Any] where A maps to B, for example
     * Pair("$health", player.getHealth())
     * @param error Whether to use error colors or not
     *
     * @return The computed [net.kyori.adventure.text.Component]
     */
    fun getMessage(
        sender: CommandSender,
        message: String,
        vararg placeholders: Pair<String, Any>,
        error: Boolean = false
    ): Component {
        // Add the "error_" prefix if it's an error message
        val errorPrefix = if (error) "error_" else ""

        // Skip the whole placeholders parsing if the message doesn't contain anything
        if (placeholders.isEmpty()) return Txt(message, "${errorPrefix}text").c

        // Final component
        val component = Component.text()

        // Convert the list of placeholder Pairs to a Map
        val map = placeholders.toMap()

        // ...Variables
        val parts = PLACEHOLDER_REGEX.split(message).filterNot { it.isBlank() }
        val matches = PLACEHOLDER_REGEX.findAll(message)

        println(parts.joinToString(""){"[$it]"})
        println(matches.toMutableList().map { it.groupValues[2] })

        val startsWithPlaceholder = message[0] in PLACEHOLDER_CHARS

        // Iterate through the matches
        for ((i, match) in matches.withIndex()) {

            // Get the key and value from the matches and the placeholders
            val key = match.groupValues[2]
            val value = map[key]


            // If the placeholder value is a Component, just skip the rest and add that to the result
            if (value is Component) {
                component.append(Txt(parts[i], "text").c)
                component.append(value)
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
            val partComponent = Txt(parts[i], "text").c

            // If the message did nit start with a placeholder prefix, append the part now
            if(!startWithPlaceholderMode) component.append(partComponent)

            // Append the value with the color
            component.append(Txt(value.toString(), "${errorPrefix}${code}").c)

            // If the message started with a placeholder prefix, append the part later
            if(startWithPlaceholderMode) component.append(partComponent)
        }

        // Append any remaining part of the message after the last placeholder
        if (matches.count() < parts.size) {
            component.append(Txt(parts.last(), "text").c)
        }

        // Return the component
        return component.build()
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
    fun sendError(sender: CommandSender, message: String, vararg placeholders: Pair<String, Any>) {
        return sendMessage(sender, message, *placeholders, error = true)
    }

    /**
     *
     * Computes a [net.kyori.adventure.text.Component] using the provided [message] and [placeholders].
     *
     * @param sender The command sender
     * @param message The message to send which includes [placeholders]. If there is a string that starts with
     * a placeholder symbol, it will be replaced with the placeholder. For example "Your health is $health"
     * @param placeholders The placeholders. A [Pair] of type [String] and [Any] where A maps to B, for example
     * Pair("$health", player.getHealth())
     *
     * @return The computed [net.kyori.adventure.text.Component]
     */
    fun getErrorMessage(sender: CommandSender, message: String, vararg placeholders: Pair<String, Any>): Component {
        return getMessage(sender, message, *placeholders, error = true)
    }
}