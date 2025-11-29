package co.akoot.plugins.bluefox.commands

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.CatCommand
import co.akoot.plugins.bluefox.api.Kolor
import co.akoot.plugins.bluefox.api.LegacyHome
import co.akoot.plugins.bluefox.extensions.*
import co.akoot.plugins.bluefox.util.Text
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.math.ceil

class HomeCommand(plugin: BlueFox) : CatCommand(plugin, "home", aliases = arrayOf("h")) {
    private val allHomeSuggestions: (ctx: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder) -> Unit =
        { ctx, builder ->
            val player = getPlayerSender(ctx)
            val homes = player?.legacyHomes?.toMutableList() ?: mutableListOf()
            player?.legacyHomeBed?.let { homes += it }
            suggest(builder, homes.map { it.name to it.location.text })
        }

    private val homeSuggestions: (ctx: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder) -> Unit =
        { ctx, builder ->
            val player = getPlayerSender(ctx)
            player?.legacyHomes?.let { homes -> suggest(builder, homes.map { it.name to it.location.text }) }
        }

    init {
        noargs {
            val player = getPlayerSender(it) ?: return@noargs false
            teleport(player, "home")
        }
        then {
            subcommand("set") {
                val player = getPlayerSender(it) ?: return@subcommand false
                set(player, "home")
            } then {
                greedyString("home name", suggestions = homeSuggestions) {
                    val player = getPlayerSender(it) ?: return@greedyString false
                    val homeName = getString(it, "home name")
                    set(player, homeName)
                }
            }
        }
        then {
            subcommand("remove") {
                val player = getPlayerSender(it) ?: return@subcommand false
                remove(player, "home")
            } then {
                greedyString("home name", suggestions = homeSuggestions) {
                    val player = getPlayerSender(it) ?: return@greedyString false
                    val homeName = getString(it, "home name")
                    remove(player, homeName)
                }
            }
        }
        then {
            subcommand("clear") {
                val player = getPlayerSender(it) ?: return@subcommand false
                player.sendMessage(
                    Kolor.WARNING("Are you sure? Type ") + Kolor.WARNING.accent("/homes clear confirm") + Kolor.WARNING(
                        " if so..."
                    )
                )
                true
            } then {
                subcommand("confirm") {
                    val player = getPlayerSender(it) ?: return@subcommand false
                    val homes = player.legacyHomes.size
                    player.legacyHomes = listOf()
                    player.sendMessage(Kolor.TEXT("Cleared ") + homes + Kolor.TEXT(" homes!"))
                    true
                }
            }
        }
        then {
            greedyString("home name", suggestions = allHomeSuggestions) {
                val player = getPlayerSender(it) ?: return@greedyString false
                val homeName = getString(it, "home name")
                teleport(player, homeName)
            }
        }
        then {
            subcommand("list") {
                val player = getPlayerSender(it) ?: return@subcommand false
                listHomes(player, 1)
            } then {
                int("page", min = 1) {
                    val player = getPlayerSender(it) ?: return@int false
                    val page = getInt(it, "page")
                    listHomes(player, page)
                    true
                }
            }
        }
    }
}

class UserHomeCommand(plugin: BlueFox) : CatCommand(plugin, "userhome", aliases = arrayOf("uh")) {
    private val allHomeSuggestions: (ctx: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder) -> Unit =
        { ctx, builder ->
            val player = getOfflinePlayer(ctx)
            val homes = player?.legacyHomes?.toMutableList() ?: mutableListOf()
            (player as? Player)?.legacyHomeBed?.let { homes += it }
            suggest(builder, homes.map { it.name to it.location.text })
        }

    private val homeSuggestions: (ctx: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder) -> Unit =
        { ctx, builder ->
            val player = getOfflinePlayer(ctx)
            suggest(builder, player?.legacyHomes?.map { it.name to it.location.text } ?: mutableListOf())
        }

    init {
        noargs {
            val sender = getSender(it)
            sender.sendMessage(Kolor.QUOTE("Uhhhhhh...").italic())
            true
        }
        then {
            offlinePlayer {
                val sender = getPlayerSender(it) ?: return@offlinePlayer false
                val player = getOfflinePlayer(it) ?: return@offlinePlayer false
                teleport(player, "home", sender)
            } then {
                subcommand("set") {
                    val sender = getSender(it)
                    val player = getOfflinePlayer(it) ?: return@subcommand false
                    set(player, "home", sender)
                } then {
                    greedyString("home name", suggestions = allHomeSuggestions) {
                        val sender = getSender(it)
                        val player = getOfflinePlayer(it) ?: return@greedyString false
                        val homeName = getString(it, "home name")
                        set(player, homeName, sender)
                    }
                }
                subcommand("remove") {
                    val sender = getSender(it)
                    val player = getOfflinePlayer(it) ?: return@subcommand false
                    remove(player, "home", sender)
                } then {
                    greedyString("home name", suggestions = homeSuggestions) {
                        val sender = getSender(it)
                        val player = getOfflinePlayer(it) ?: return@greedyString false
                        val homeName = getString(it, "home name")
                        remove(player, homeName, sender)
                    }
                }
            } then {
                greedyString("home name", suggestions = allHomeSuggestions) {
                    val sender = getSender(it)
                    val player = getOfflinePlayer(it) ?: return@greedyString false
                    val homeName = getString(it, "home name")
                    teleport(player, homeName, sender)
                }
            } then {
                subcommand("list") {
                    val sender = getSender(it)
                    val player = getOfflinePlayer(it) ?: return@subcommand false
                    listHomes(player, 1, sender)
                } then {
                    int("page", min = 1) {
                        val sender = getSender(it)
                        val player = getOfflinePlayer(it) ?: return@int false
                        val page = getInt(it, "page")
                        listHomes(player, page, sender)
                    }
                }
            }
        }
    }
}

class SetHomeCommand(plugin: BlueFox): CatCommand(plugin, "sethome", aliases = arrayOf("sh")) {
    private val homeSuggestions: (ctx: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder) -> Unit =
        { ctx, builder ->
            val player = getPlayerSender(ctx)
            player?.legacyHomes?.let { homes -> suggest(builder, homes.map { it.name to it.location.text }) }
        }
    init {
        noargs {
            val player = getPlayerSender(it) ?: return@noargs false
            set(player, "home", player)
        }
        then {
            greedyString("home name", suggestions = homeSuggestions) {
                val player = getPlayerSender(it) ?: return@greedyString false
                val homeName = getString(it, "home name")
                set(player, homeName, player)
            }
        }
    }
}

class DelHomeCommand(plugin: BlueFox): CatCommand(plugin, "delhome", aliases = arrayOf("dh")) {
    private val homeSuggestions: (ctx: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder) -> Unit =
        { ctx, builder ->
            val player = getPlayerSender(ctx)
            player?.legacyHomes?.let { homes -> suggest(builder, homes.map { it.name to it.location.text }) }
        }
    init {
        noargs {
            val player = getPlayerSender(it) ?: return@noargs false
            remove(player, "home", player)
        }
        then {
            greedyString("home name", suggestions = homeSuggestions) {
                val player = getPlayerSender(it) ?: return@greedyString false
                val homeName = getString(it, "home name")
                remove(player, homeName, player)
            }
        }
    }
}

class HomesCommand(plugin: BlueFox): CatCommand(plugin, "homes") {
    init {
        noargs {
            val player = getPlayerSender(it) ?: return@noargs false
            listHomes(player, 1)
        }
        then {
            int("page", min = 1) {
                val player = getPlayerSender(it) ?: return@int false
                val page = getInt(it, "page")
                listHomes(player, page)
            }
        }
    }
}

private const val homesPerPage = 10
private fun listHomes(player: OfflinePlayer, page: Int, sender: CommandSender? = player as? Player): Boolean {
    val self = player == sender
    val command = if (self) "home" else "userhome"
    val homes = player.legacyHomes
    val homesSize = homes.size
    val totalPages = ceil(homesSize.toDouble() / homesPerPage).toInt()
    if (homesSize == 0) {
        sender?.sendMessage(
            Kolor.WARNING("You are homeless!") + Text.newline + (Kolor.QUOTE("- Set one using ") + Kolor.QUOTE.alt(
                "/sethome"
            ) + Kolor.QUOTE(" maybe?")).italic()
        )
        return false
    }
    if (page > totalPages) {
        sender?.sendMessage(
            Kolor.WARNING("Nice fail, you have like ") + Kolor.WARNING.number(totalPages.toString()) + Kolor.WARNING(
                " page(s) of homes MAX..."
            )
        )
        return false
    }
    sender?.sendMessage(Kolor.TEXT("[") + Kolor.PLAYER(player.name ?: "Player") + Kolor.ACCENT("'s homes") + Kolor.TEXT("]"))
    val start = (page - 1) * homesPerPage
    val end = minOf(start + homesPerPage, homesSize)
    homes.subList(start, end).stream()
        .map { Kolor.TEXT("• ") + Kolor.ALT(it.name).hover(it.location.text).execute("/$command ${it.name}") }
        .forEach { sender?.sendMessage(it) }
    sender?.sendMessage(
        (if (page > 1) Kolor.TEXT("◀ ").execute("/$command list ${page - 1}") else Kolor.QUOTE("◀ ")
            .execute(null)) + Kolor.ACCENT("Page ") + Kolor.NUMBER(page) + Kolor.TEXT(" / ") + Kolor.NUMBER(totalPages) + (if (page < totalPages) Kolor.TEXT(
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
    val home = get(player, homeName) ?: return false
    sender?.sendMessage(
        Kolor.TEXT("Teleported ") + (if (homeName == "home") Text() else Kolor.TEXT("to ")) + Kolor.ACCENT(
            homeName
        ) + "!"
    )
    if (sender !is Player) {
        sender?.sendMessage("Player only!")
        return false
    }
    return sender.teleport(home)
}

private fun remove(player: OfflinePlayer, homeName: String, sender: CommandSender? = player as? Player): Boolean {
    val self = player == sender
    if (homeName == "bed") {
        sender?.sendMessage(
            Kolor.WARNING("If you want to delete ${if (self) "your" else "${player.name}'s"} home ") + Kolor.WARNING.accent(
                homeName
            ) + Kolor.WARNING(", just break it!")
        )
        return false
    }
    val home = get(player, homeName) ?: return false
    val removed = player.deleteLegacyHome(homeName)
    if (removed) {
        sender?.sendMessage(
            Kolor.TEXT("Removed ${if (self) "" else "${player.name}'s "}home ") + Kolor.ACCENT(homeName) + Kolor.TEXT(
                " at "
            ) + home.location.text + "!"
        )
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
    sender.sendMessage(
        Kolor.TEXT(if (replaced) "Moved " else "Set ") + (if (self) "" else "${player.name}'s ") + (if (homeName == "home") Text() else Kolor.TEXT(
            "home "
        )) + Kolor.ACCENT(homeName) + Kolor.TEXT(" to ") + home.location.text + "!"
    )
    return true
}

private fun suggestSetHome(player: OfflinePlayer, homeName: String, sender: CommandSender? = player as? Player) {
    val self = player == sender
    val command = if (self) "home" else "userhome ${player.name}"
    val suggestion = if (homeName == "home") "/$command set" else "/$command set $homeName"
    sender?.sendMessage(
        Kolor.WARNING("${if (self) "You do" else "${player.name} does"} not have a ") + (if (homeName == "home") Kolor.WARNING.accent(
            homeName
        ) else Kolor.WARNING("home named ") + Kolor.WARNING.accent(homeName)) + Kolor.WARNING("!")
                + Text.newline + (Kolor.QUOTE("- Set it with ") + Kolor.QUOTE.alt(suggestion)).italic()
            .suggest(suggestion)
    )
}

private fun suggestSetBed(player: OfflinePlayer, sender: CommandSender? = player as? Player) {
    val self = player == sender
    sender?.sendMessage(
        Kolor.WARNING("If you want set ${if (self) "" else "${player.name}'s"} a home ") + Kolor.WARNING.accent(
            "bed"
        ) + Kolor.WARNING(", just ${if (self) "" else "make them"} sleep in one!")
    )
}