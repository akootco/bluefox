package co.akoot.plugins.bluefox.commands

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.CatCommand
import co.akoot.plugins.bluefox.api.Kolor
import co.akoot.plugins.bluefox.api.LegacyHome
import co.akoot.plugins.bluefox.extensions.deleteLegacyHome
import co.akoot.plugins.bluefox.extensions.getLegacyHome
import co.akoot.plugins.bluefox.extensions.invoke
import co.akoot.plugins.bluefox.extensions.legacyHomeBed
import co.akoot.plugins.bluefox.extensions.legacyHomes
import co.akoot.plugins.bluefox.extensions.sendMessage
import co.akoot.plugins.bluefox.extensions.setLegacyHome
import co.akoot.plugins.bluefox.extensions.teleport
import co.akoot.plugins.bluefox.extensions.text
import co.akoot.plugins.bluefox.util.Text
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player
import kotlin.collections.map
import kotlin.math.ceil

class HomeCommand(plugin: BlueFox): CatCommand(plugin, "home") {

    private val allHomeSuggestions: (ctx: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder) -> Unit = { ctx, builder ->
        val player = getPlayerSender(ctx)
        player?.legacyHomes?.plus(player.legacyHomeBed)?.filterNotNull()?.let { home ->  suggest(builder, home.map { it.name to it.location.text }) }
    }

    private val homeSuggestions: (ctx: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder) -> Unit = { ctx, builder ->
        val player = getPlayerSender(ctx)
        player?.legacyHomes?.let { home ->  suggest(builder, home.map { it.name to it.location.text }) }
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
                player.sendMessage(Kolor.WARNING("Are you sure? Type ") + Kolor.WARNING.accent("/homes clear confirm") + Kolor.WARNING(" if so..."))
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

    private val homesPerPage = 10
    private fun listHomes(player: Player, page: Int): Boolean {
        val homes = player.legacyHomes
        val homesSize = homes.size
        println("homesSize / homesPerPage: ${homesSize / homesPerPage}")
        val totalPages = ceil(homesSize.toDouble() / homesPerPage).toInt()
        if(homesSize == 0) {
            player.sendMessage(Kolor.WARNING("You are homeless!") + Text.newline + (Kolor.QUOTE("- Set one using ") + Kolor.QUOTE.alt("/sethome") + Kolor.QUOTE(" maybe?")).italic())
            return false
        }
        if(page > totalPages) {
            player.sendMessage(Kolor.WARNING("Nice fail, you have like ") + Kolor.WARNING.number(totalPages.toString()) + Kolor.WARNING(" page(s) of homes MAX..."))
            return false
        }
        val start = (page - 1) * homesPerPage
        val end = minOf(start + homesPerPage, homesSize)
        homes.subList(start, end).stream()
            .map { Kolor.TEXT("• ") + Kolor.ALT(it.name).hover(it.location.text).execute("/home ${it.name}") }
            .forEach(player::sendMessage)
        player.sendMessage((if(page > 1) Kolor.TEXT("◀ ").execute("/home list ${page - 1}") else Kolor.QUOTE("◀ ").execute(null)) + Kolor.ACCENT("Page ") + Kolor.NUMBER(page) + Kolor.TEXT(" / ") + Kolor.NUMBER(totalPages) + (if(page < totalPages) Kolor.TEXT(" ▶").execute("/home list ${page + 1}") else Kolor.QUOTE(" ▶").execute(null)))
        return true
    }

    private fun get(player: Player, homeName: String): LegacyHome? {
        val home = player.getLegacyHome(homeName)
        if(home == null) {
            if(homeName == "bed") {
                suggestSetBed(player)
            } else {
                suggestSetHome(player, homeName)
            }
        }
        return home
    }

    private fun teleport(player: Player, homeName: String): Boolean {
        val home = get(player, homeName) ?: return false
        player.sendMessage(Kolor.TEXT("Teleported ") + (if(homeName == "home") Text() else Kolor.TEXT("to ")) + Kolor.ACCENT(homeName) + "!")
        return player.teleport(home)
    }

    private fun remove(player: Player, homeName: String): Boolean {
        if(homeName == "bed") {
            player.sendMessage(Kolor.WARNING("If you want to delete your home ") + Kolor.WARNING.accent(homeName) + Kolor.WARNING(", just break it!"))
            return false
        }
        val home = get(player, homeName) ?: return false
        val removed = player.deleteLegacyHome(homeName)
        if(removed) {
            player.sendMessage(Kolor.TEXT("Removed home ") + Kolor.ACCENT(homeName) + Kolor.TEXT(" at ") + home.location.text + "!")
        } else {
            suggestSetHome(player, homeName)
        }
        return removed
    }

    private fun set(player: Player, homeName: String): Boolean {
        if(homeName == "bed") {
            suggestSetBed(player)
            return false
        }
        val home = LegacyHome(homeName, player.location)
        val replaced = player.setLegacyHome(home)
        player.sendMessage(Kolor.TEXT(if(replaced) "Moved " else "Set ") + (if(homeName == "home") Text() else Kolor.TEXT("home ")) + Kolor.ACCENT(homeName) + Kolor.TEXT(" to ") + home.location.text + "!")
        return true
    }

    private fun suggestSetHome(player: Player, homeName: String) {
        val suggestion = if(homeName == "home") "/sethome" else "/sethome $homeName"
        player.sendMessage(Kolor.WARNING("You do not have a ") + (if(homeName == "home") Kolor.WARNING.accent(homeName) else Kolor.WARNING("home named ") + Kolor.WARNING.accent(homeName)) + Kolor.WARNING("!")
                + Text.newline + (Kolor.QUOTE("- Set it with ") + Kolor.QUOTE.alt(suggestion)).italic().suggest(suggestion))
    }

    private fun suggestSetBed(player: Player) {
        player.sendMessage(Kolor.WARNING("If you want set a home ") + Kolor.WARNING.accent("bed") + Kolor.WARNING(", just sleep in one!"))
    }
}