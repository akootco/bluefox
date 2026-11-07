package co.akoot.plugins.bluefox.api.changelog

import co.akoot.plugins.bluefox.util.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component

object ChangelogManager {
    private val changelogs = mutableListOf<Changelog>()

    fun clear() = changelogs.clear()
    fun register(changelog: Changelog) {
        changelogs.add(changelog)
    }

    fun getChangelog(id: String): Changelog? = changelogs.find { it.id == id }
    fun log(id: String, change: Change) {
        getChangelog(id)?.add(change)
    }

    fun logAddition(id: String, addition: String, hidden: Boolean = false) =
        log(id, Change(Change.Type.ADDITION, addition, hidden))

    fun logDeletion(id: String, deletion: String, hidden: Boolean = false) =
        log(id, Change(Change.Type.DELETION, deletion, hidden))

    fun logFix(id: String, fix: String, hidden: Boolean = false) =
        log(id, Change(Change.Type.FIX, fix, hidden))

    fun logChange(id: String, change: String, hidden: Boolean = false) =
        log(id, Change(Change.Type.CHANGE, change, hidden))

    fun send(audience: Audience) {
        val message = mutableListOf<Component>()
        message += primary("=== Changelog - July 7th, 2024 ===\n")
        for (changelog in changelogs) {
            message += changelog.color?.let { getColor(it) } + "[ ${changelog.title} ]"
            message += Component.newline()

            val changes = changelog.changes.filter { it.canBeSeenBy(audience) }
                .joinToString("\n") { "&${it.type.color}${it.type.symbol} &r${it.message}" }

            message += changes.parse()
            message += Component.newline()

            message += Component.newline()
        }
        audience.sendMessage(message.zip)
    }

    fun send(channel: TextChannel) {
        val embeds = mutableListOf<MessageEmbed>()
        for (changelog in changelogs) {
            val builder = EmbedBuilder()
            builder.setAuthor(changelog.title, null, changelog.icon)
            builder.setColor(changelog.color?.let { getColor(it) }?.value()?.let { java.awt.Color(it) })

            val additions = changelog.changes.filter { !it.hidden && it.type == Change.Type.ADDITION }
                .joinToString("\n") { "${it.type.icon} ${it.message}" }

            val deletions = changelog.changes.filter { !it.hidden && it.type == Change.Type.DELETION }
                .joinToString("\n") { "${it.type.icon} ${it.message}" }

            val changes = changelog.changes.filter { !it.hidden && it.type == Change.Type.CHANGE }
                .joinToString("\n") { "${it.type.icon} ${it.message}" }

            val fixes = changelog.changes.filter { !it.hidden && it.type == Change.Type.FIX }
                .joinToString("\n") { "${it.type.icon} ${it.message}" }

            if (additions.isNotBlank()) builder.addField("ADDITIONS", additions, false)
            if (deletions.isNotBlank()) builder.addField("DELETIONS", deletions, false)
            if (changes.isNotBlank()) builder.addField("CHANGES", changes, false)
            if (fixes.isNotBlank()) builder.addField("FIXES", fixes, false)

            try {
                builder.setImage(changelog.image)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            embeds += builder.build()
        }
        channel.sendMessage("# :date: Changelog - July 7th, 2024").queue()
        channel.sendMessageEmbeds(embeds).queue()
    }

    data class Plugin(val id: String, val name: String, val icon: String?, val color: String)
}