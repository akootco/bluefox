package co.akoot.plugins.bluefox.commands

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.CommandHelp
import co.akoot.plugins.bluefox.api.CatCommand
import co.akoot.plugins.bluefox.api.Kolor
import co.akoot.plugins.bluefox.api.LegacyHome
import co.akoot.plugins.bluefox.extensions.*
import co.akoot.plugins.bluefox.util.Text
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.math.ceil

class HomeCommand(plugin: BlueFox) : CatCommand(plugin, "home", aliases = arrayOf("h")) {
    init {
        noargs {
            permissionCheck(it) ?: return@noargs false
            val player = getPlayerSender(it) ?: return@noargs false
            teleport(player, "home")
        }
        then {
            greedyString("home name", { ctx, builder -> suggest(builder, homeSuggestions(getPlayerSender(ctx), true)) }) {
                permissionCheck(it) ?: return@greedyString false
                val player = getPlayerSender(it) ?: return@greedyString false
                val homeName = getString(it, "home name")
                teleport(player, homeName)
            }
        }
    }
}

class UserHomeCommand(plugin: BlueFox) : CatCommand(plugin, "userhome", aliases = arrayOf("uh")) {
    init {
        noargs {
            val sender = getSender(it)
            sender.sendMessage(Kolor.QUOTE("Uhhhhhh...").italic())
            true
        }
        then {
            offlinePlayer {
                permissionCheck(it)
                val sender = getPlayerSender(it) ?: return@offlinePlayer false
                val player = getOfflinePlayer(it) ?: return@offlinePlayer false
                teleport(player, "home", sender)
            } then {
                greedyString("home name", { ctx, builder -> suggest(builder, homeSuggestions(getOfflinePlayer(ctx), true)) }) {
                    permissionCheck(it)
                    val sender = getSender(it)
                    val player = getOfflinePlayer(it) ?: return@greedyString false
                    val homeName = getString(it, "home name")
                    teleport(player, homeName, sender)
                }
            }
        }
    }
}

class SetHomeCommand(plugin: BlueFox) : CatCommand(plugin, "sethome", aliases = arrayOf("sh")) {
    init {
        noargs {
            val player = getPlayerSender(it) ?: return@noargs false
            set(player, "home", player)
        }
        then {
            greedyString("home name", { ctx, builder -> suggest(builder, homeSuggestions(getPlayerSender(ctx))) }) {
                val player = getPlayerSender(it) ?: return@greedyString false
                val homeName = getString(it, "home name")
                set(player, homeName, player)
            }
        }
    }
}

class DelHomeCommand(plugin: BlueFox) : CatCommand(plugin, "delhome", aliases = arrayOf("dh")) {
    init {
        noargs {
            val player = getPlayerSender(it) ?: return@noargs false
            remove(player, "home", player)
        }
        then {
            greedyString("home name", { ctx, builder -> suggest(builder, homeSuggestions(getPlayerSender(ctx))) }) {
                val player = getPlayerSender(it) ?: return@greedyString false
                val homeName = getString(it, "home name")
                remove(player, homeName, player)
            }
        }
    }
}

class HomesCommand(plugin: BlueFox) : CatCommand(plugin, "homes") {
    init {
        noargs {
            val player = getPlayerSender(it) ?: return@noargs false
            listHomes(player, 1)
        }
        then {
            subcommand("set") {
                permissionCheck(it, "set") ?: return@subcommand false
                val player = getPlayerSender(it) ?: return@subcommand false
                set(player, "home")
            } then {
                greedyString("home name", { ctx, builder -> suggest(builder, homeSuggestions(getPlayerSender(ctx))) }) {
                    permissionCheck(it, "set.named") ?: return@greedyString false
                    val player = getPlayerSender(it) ?: return@greedyString false
                    val homeName = getString(it, "home name")
                    set(player, homeName)
                }
            }
        }
        then {
            subcommand("remove") {
                permissionCheck(it, "remove") ?: return@subcommand false
                val player = getPlayerSender(it) ?: return@subcommand false
                remove(player, "home")
            } then {
                greedyString("home name", { ctx, builder -> suggest(builder, homeSuggestions(getPlayerSender(ctx))) }) {
                    permissionCheck(it, "remove.named") ?: return@greedyString false
                    val player = getPlayerSender(it) ?: return@greedyString false
                    val homeName = getString(it, "home name")
                    remove(player, homeName)
                }
            }
        }
        then {
            subcommand("clear") {
                permissionCheck(it, "clear") ?: return@subcommand false
                val player = getPlayerSender(it) ?: return@subcommand false
                clear(player)
            } then {
                subcommand("confirm") {
                    permissionCheck(it, "clear") ?: return@subcommand false
                    val player = getPlayerSender(it) ?: return@subcommand false
                    clear(player, true)
                }
            }
        }
        then {
            subcommand("list") {
                permissionCheck(it, "list") ?: return@subcommand false
                val player = getPlayerSender(it) ?: return@subcommand false
                listHomes(player, 1)
            } then {
                int("page", min = 1) {
                    permissionCheck(it, "list") ?: return@int false
                    val player = getPlayerSender(it) ?: return@int false
                    val page = getInt(it, "page")
                    listHomes(player, page)
                }
            }
        }
        then {
            subcommand("clean") {
                permissionCheck(it, "clean") ?: return@subcommand false
                val player = getPlayerSender(it) ?: return@subcommand false
                player.sendMessage(CommandHelp("Usage: ").usage("/home clean").usage("query", literal = false, final = true).text)
                true
            } then {
                word("query mode", { ctx, builder -> suggest(builder, queryModes) }) {
                    val player = getPlayerSender(it) ?: return@word false
                    player.sendMessage(CommandHelp("Usage: ").usage("/homes clean").usage("query mode", literal = false).usage("query", literal = false).text)
                    false
                } then {
                    greedyString("query") {
                        permissionCheck(it, "clean") ?: return@greedyString false
                        val player = getPlayerSender(it) ?: return@greedyString false
                        val queryModeString = getString(it, "query mode")
                        val queryMode = QueryMode.of(queryModeString)
                        if (queryMode == null) {
                            player.sendMessage(Kolor.WARNING("Invalid query mode: ") + Kolor.WARNING.accent(queryModeString))
                            return@greedyString false
                        }
                        val query = getString(it, "query")
                        val confirm = query.contains("--confirm")
                        clean(player, queryMode, query)
                    }
                }
            }
        }
    }
}

class UserHomesCommand(plugin: BlueFox): CatCommand(plugin, "userhomes", aliases = arrayOf("uhh")) {
    init {
        noargs {
            false
        }
        then {
            offlinePlayer {
                permissionCheck(it, "list")
                val sender = getSender(it)
                val player = getOfflinePlayer(it) ?: return@offlinePlayer false
                listHomes(player, 1, sender)
            } then {
                subcommand("remove") {
                    permissionCheck(it, "remove")
                    val sender = getSender(it)
                    val player = getOfflinePlayer(it) ?: return@subcommand false
                    remove(player, "home", sender)
                } then {
                    greedyString(
                        "home name",
                        { ctx, builder -> suggest(builder, homeSuggestions(getOfflinePlayer(ctx))) }) {
                        permissionCheck(it, "remove")
                        val sender = getSender(it)
                        val player = getOfflinePlayer(it) ?: return@greedyString false
                        val homeName = getString(it, "home name")
                        remove(player, homeName, sender)
                    }
                }
            } then {
                subcommand("set") {
                    permissionCheck(it, "set")
                    val sender = getSender(it)
                    val player = getOfflinePlayer(it) ?: return@subcommand false
                    set(player, "home", sender)
                } then {
                    greedyString("home name", { ctx, builder -> suggest(builder, homeSuggestions(getOfflinePlayer(ctx), true)) }) {
                        permissionCheck(it, "set")
                        val sender = getSender(it)
                        val player = getOfflinePlayer(it) ?: return@greedyString false
                        val homeName = getString(it, "home name")
                        set(player, homeName, sender)
                    }
                }
            } then {
                subcommand("list") {
                    permissionCheck(it, "list")
                    val sender = getSender(it)
                    val player = getOfflinePlayer(it) ?: return@subcommand false
                    listHomes(player, 1, sender)
                } then {
                    int("page", min = 1) {
                        permissionCheck(it, "list")
                        val sender = getSender(it)
                        val player = getOfflinePlayer(it) ?: return@int false
                        val page = getInt(it, "page")
                        listHomes(player, page, sender)
                    }
                }
            } then {
                subcommand("clear") {
                    permissionCheck(it, "clear")
                    val sender = getSender(it)
                    val player = getOfflinePlayer(it) ?: return@subcommand false
                    clear(player, false, sender)
                } then {
                    subcommand("confirm") {
                        val sender = getSender(it)
                        val player = getOfflinePlayer(it) ?: return@subcommand false
                        clear(player, true, sender)
                    }
                }
            } then {
                subcommand("clean") {
                    permissionCheck(it, "clean") ?: return@subcommand false
                    val sender = getSender(it)
                    sender.sendMessage(CommandHelp("Usage: ").usage("/userhomes").usage("player", literal = false).usage("clean").usage("query mode", literal = false).usage("query", literal = false).text)
                    true
                } then {
                    word("query mode", suggestions = { ctx, builder -> suggest(builder, queryModes) }) {
                        permissionCheck(it, "clean") ?: return@word false
                        val sender = getSender(it)
                        sender.sendMessage(CommandHelp("Usage: ").usage("/userhomes").usage("player", literal = false).usage("clean").usage("query mode", literal = false).usage("query", literal = false).text)
                        true
                    } then {
                        greedyString("query") {
                            permissionCheck(it, "clean") ?: return@greedyString false
                            val sender = getSender(it)
                            val player = getOfflinePlayer(it) ?: return@greedyString false
                            val queryModeString = getString(it, "query mode")
                            val queryMode = QueryMode.of(queryModeString)
                            if(queryMode == null) {
                                sender.sendMessage(Kolor.WARNING("Invalid query mode: ") + Kolor.WARNING.accent(queryModeString))
                                return@greedyString false
                            }
                            val query = getString(it, "query")
                            clean(player, queryMode, query, sender)
                        }
                    }
                }
            }
        }
    }
}

private enum class QueryMode(val argument: String, val description: String) {
    CONTAINS("has", "Select homes that have the specified text anywhere in the name"),
    STARTS_WITH("startsWith", "Select homes start with the specified text"),
    ENDS_WITH("endsWith", "Select homes that end with the specified text"),
    NOT_CONTAINS("without", "Select homes that DO NOT have the specified text anywhere in the name"),
    LONGER_THAN("longerThan", "Select homes that have a name longer than the specified number"),
    SHORTER_THAN("shorterThan", "Select homes that have a name shorter than the specified number"),
    IN("in", "Select homes that are in the specified world"),
    NOT_IN("notIn", "Select homes that ARE NOT in the specified world"),
    ;
    companion object {
        fun of(string: String): QueryMode? {
            return QueryMode.entries.find { it.argument == string }
        }
    }
    fun matches(home: LegacyHome, query: String): Boolean {
        return when (this) {
            CONTAINS -> home.name.contains(query, true)
            STARTS_WITH -> home.name.startsWith(query, true)
            ENDS_WITH -> home.name.endsWith(query, true)
            NOT_CONTAINS -> !home.name.contains(query, true)
            LONGER_THAN -> home.name.length > query.toInt()
            SHORTER_THAN -> home.name.length < query.toInt()
            IN -> home.location.world.name == query
            NOT_IN -> home.location.world.name != query
        }
    }
}
private val queryModes: List<Pair<String, Text>> = QueryMode.entries.map {
    it.argument to Text(it.description)
}
private fun clean(player: OfflinePlayer, queryMode: QueryMode, queryString: String, sender: CommandSender? = player as? Player): Boolean {
    val confirm = "--confirm" in queryString
    val query = queryString.replace("--confirm", "").trim()
    val self = player == sender
    val homes = player.legacyHomes
    val found = homes.filter { queryMode.matches(it, query) }
    val foundSize = found.size
    val command = if(self) "/homes clean ${queryMode.argument} $query --confirm" else "/userhomes ${player.username} clean ${queryMode.argument} $query --confirm"
    val delCommand = if(self) "delhome" else "userhomes ${player.username} remove"
    val message =
        if(foundSize == 0) Kolor.WARNING("No homes found!")
        else if(!confirm) Kolor.TEXT("[") +  Kolor.ACCENT("Found ") + Kolor.NUMBER.accent(foundSize) + Kolor.ACCENT(" homes") + Kolor.TEXT("]\n") + Text.list(found.map { homeItem(it, delCommand) }, "\n") + Kolor.TEXT("\nType ") + Kolor.QUOTE(command).italic().suggest(command)
        else Kolor.TEXT("Removed ") + foundSize + Kolor.TEXT(" homes!")
    sender?.sendMessage(message)
    if(confirm) player.legacyHomes = homes.filterNot { queryMode.matches(it, query) }
    return true
}

private fun homeSuggestions(player: OfflinePlayer?, includeBed: Boolean = false): List<Pair<String, Text>> {
    val homes = player?.legacyHomes?.toMutableList() ?: mutableListOf()
    if(includeBed) (player as? Player)?.legacyHomeBed?.let { homes += it }
    return homes.map { it.name to it.location.text }
}

private fun clear(player: OfflinePlayer, confirm: Boolean = false, sender: CommandSender? = player as? Player): Boolean {
    val self = player == sender
    val homesSize = player.legacyHomes.size
    val message = if(self) {
        if(confirm) Kolor.TEXT("Removed all ") + homesSize + Kolor.TEXT(" of your homes!") else Kolor.WARNING("Are you sure you want to remove ") + Kolor.WARNING.number("all $homesSize") + Kolor.WARNING(" of your homes? Type ") + Kolor.WARNING.accent("/home clear confirm") + Kolor.WARNING(" if so...")
    } else {
        if(confirm) Kolor.TEXT("Removed all ") + homesSize + Kolor.TEXT(" of ") + player.textPosessive() + Kolor.TEXT(" homes!") else Kolor.WARNING("Are you sure you want to remove ") + Kolor.WARNING.number("all $homesSize") + Kolor.WARNING(" of ") + player.textPosessive(Kolor.WARNING + Kolor.PLAYER) + Kolor.WARNING(" homes? Type ") + Kolor.WARNING.accent("/userhome ${player.username} clear confirm") + Kolor.WARNING(" if so...")
    }
    sender?.sendMessage(message)
    if(confirm) player.legacyHomes = listOf()
    return true
}

private const val homesPerPage = 10
private fun listHomes(player: OfflinePlayer, page: Int, sender: CommandSender? = player as? Player): Boolean {
    val self = player == sender
    val command = if (self) "homes" else "userhome ${player.username}"
    val homes = player.legacyHomes
    val homesSize = homes.size
    val totalPages = ceil(homesSize.toDouble() / homesPerPage).toInt()
    val setCommand = if (self) {
        "sethome"
    } else {
        "userhome ${player.username} set"
    }
    if (homesSize == 0) {
        val message = if (self) {
            Kolor.WARNING("You are homeless!") + Text.newline + (Kolor.QUOTE("- Set one using ") + Kolor.QUOTE.alt("/$setCommand") + Kolor.QUOTE(
                " maybe?"
            )).italic().suggest(setCommand)
        } else {
            player.text(Kolor.WARNING + Kolor.PLAYER) + Kolor.WARNING(" is homeless!") + Text.newline + (Kolor.QUOTE("- Set one for them using ") + Kolor.QUOTE.alt(
                "/$setCommand"
            ) + Kolor.QUOTE(" maybe?")).italic().suggest(setCommand)
        }
        sender?.sendMessage(message)
        return false
    }
    if (page > totalPages) {
        val message = if (self) {
            Kolor.WARNING("Nice fail, you have like ") + Kolor.WARNING.number(totalPages.toString()) + Kolor.WARNING(
                " page(s) of homes MAX..."
            )
        } else {
            Kolor.WARNING("Nice fail, ") + player.text(Kolor.WARNING + Kolor.PLAYER) + Kolor.WARNING(" has like ") + Kolor.WARNING.number(
                totalPages.toString()
            ) + Kolor.WARNING(
                " page(s) of homes MAX..."
            )
        }
        sender?.sendMessage(message)
        return false
    }
    sender?.sendMessage(Kolor.TEXT("[") + player.textPosessive() + Kolor.ACCENT(" homes") + Kolor.TEXT("]"))
    val start = (page - 1) * homesPerPage
    val end = minOf(start + homesPerPage, homesSize)
    homes.subList(start, end).stream()
        .map { homeItem(it, command) }
        .forEach { sender?.sendMessage(it) }
    sender?.sendMessage(
        (if (page > 1) Kolor.TEXT("◀ ").execute("/$command list ${page - 1}") else Kolor.QUOTE("◀ ")
            .execute(null)) + (Kolor.ACCENT("Page ") + Kolor.NUMBER(page) + Kolor.TEXT(" / ") + Kolor.NUMBER(totalPages)).suggest(
            "/$command list "
        ) + (if (page < totalPages) Kolor.TEXT(
            " ▶"
        ).execute("/$command list ${page + 1}") else Kolor.QUOTE(" ▶").execute(null))
    )
    return true
}

private fun get(player: OfflinePlayer, homeName: String, sender: CommandSender? = player as? Player): LegacyHome? {
    val home = player.getLegacyHome(homeName)
    if (home == null) {
        if (homeName == "bed") {
            suggestSetBed(player, sender)
        } else {
            suggestSetHome(player, homeName, sender)
        }
    }
    return home
}

private fun teleport(player: OfflinePlayer, homeName: String, sender: CommandSender? = player as? Player): Boolean {
    val home = get(player, homeName, sender) ?: return false
    val isHome = homeName == "home"
    val message = if (player == sender) {
        Kolor.TEXT("Teleported ") + (if (isHome) Text() else Kolor.TEXT("to ")) + Kolor.ACCENT(homeName) + "!"
    } else {
        Kolor.TEXT("Teleported to ") + Kolor.PLAYER(
            player.name ?: "Player"
        ) + Kolor.TEXT("'s ") + (if (isHome) Text() else Kolor.TEXT("home ")) + Kolor.ACCENT(homeName) + "!"
    }
    sender?.sendMessage(message)
    if (sender !is Player) {
        sender?.sendMessage("Player only!")
        return false
    }
    return sender.teleport(home)
}

private fun remove(player: OfflinePlayer, homeName: String, sender: CommandSender? = player as? Player): Boolean {
    val self = player == sender
    if (homeName == "bed") {
        val message = if (self) {
            Kolor.WARNING("If you want to remove your home ") + Kolor.WARNING.accent(homeName) + Kolor.WARNING(", just break it!")
        } else {
            Kolor.WARNING("If you want to remove ") + player.textPosessive(Kolor.WARNING) + Kolor.WARNING(" home ") + Kolor.WARNING.accent(
                homeName
            ) + Kolor.WARNING(", just make them break it!")
        }
        sender?.sendMessage(message)
        return false
    }
    val home = get(player, homeName) ?: return false
    val removed = player.removeLegacyHome(homeName)
    if (removed) {
        val message = if (self) {
            Kolor.TEXT("Removed home ") + Kolor.ACCENT(homeName) + Kolor.TEXT(" at ") + home.location.text + "!"
        } else {
            Kolor.TEXT("Removed ") + player.textPosessive() + Kolor.TEXT(" home ") + Kolor.ACCENT(homeName) + Kolor.TEXT(
                " at "
            ) + home.location.text + "!"
        }
        sender?.sendMessage(message)
    } else {
        suggestSetHome(player, homeName)
    }
    return removed
}

private fun set(player: OfflinePlayer, homeName: String, sender: CommandSender? = player as? Player): Boolean {
    if (homeName == "bed") {
        suggestSetBed(player, sender)
        return false
    }
    val self = player == sender
    if (sender !is Player) {
        sender?.sendMessage("Player only!")
        return false
    }
    val home = LegacyHome(homeName, sender.location)
    val replaced = player.setLegacyHome(home)
    val isHome = homeName == "home"
    val message = if (self) {
        Kolor.TEXT(if (replaced) "Moved " else "Set ") + (if (isHome) Text() else Kolor.TEXT("home ")) + Kolor.ACCENT(
            homeName
        ) + Kolor.TEXT(" to ") + home.location.text + "!"
    } else {
        Kolor.TEXT(if (replaced) "Moved " else "Set ") + player.textPosessive() + (if (isHome) Text(" ") else Kolor.TEXT(
            " home "
        )) + Kolor.ACCENT(homeName) + Kolor.TEXT(" to ") + home.location.text + "!"
    }
    sender.sendMessage(message)
    return true
}

private fun homeItem(home: LegacyHome, command: String): Text {
    return Kolor.TEXT("• ") + Kolor.ALT(home.name).hover(home.location.text).execute("/$command ${home.name}")
}

private fun suggestSetHome(player: OfflinePlayer, homeName: String, sender: CommandSender? = player as? Player) {
    val self = player == sender
    val command = if (self) "sethome" else "userhome ${player.name} set"
    val suggestion = if (homeName == "home") "/$command " else "/$command $homeName "
    val isHome = homeName == "home"
    val message = if (self) {
        Kolor.WARNING("You do not have a ") + (if (isHome) Kolor.WARNING.accent(homeName) else Kolor.WARNING("home named ") + Kolor.WARNING.accent(
            homeName
        )) + Kolor.WARNING("!") + Text.newline + (Kolor.QUOTE("- Set it with ") + Kolor.QUOTE.alt(suggestion)).italic()
            .suggest(suggestion)
    } else {
        player.text(Kolor.WARNING + Kolor.PLAYER) + Kolor.WARNING(" does not have a ") + (if (isHome) Kolor.WARNING.accent(
            homeName
        ) else Kolor.WARNING("home named ") + Kolor.WARNING.accent(homeName)) + Kolor.WARNING("!") + Text.newline + (Kolor.QUOTE(
            "- Set it with "
        ) + Kolor.QUOTE.alt(suggestion)).italic().suggest(suggestion)
    }
    sender?.sendMessage(message)
}

private fun suggestSetBed(player: OfflinePlayer, sender: CommandSender? = player as? Player) {
    val self = player == sender
    val message = if (self) {
        Kolor.WARNING("If you want to set a home ") + Kolor.WARNING.accent("bed") + Kolor.WARNING(", just sleep in one!")
    } else {
        Kolor.WARNING("If you want to set a home ") + Kolor.WARNING.accent("bed") + Kolor.WARNING(" for ") + player.text(
            Kolor.WARNING + Kolor.PLAYER
        ) + Kolor.WARNING(", just make them sleep in one!")
    }
    sender?.sendMessage(message)
}