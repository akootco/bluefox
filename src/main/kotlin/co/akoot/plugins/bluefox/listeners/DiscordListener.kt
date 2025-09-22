package co.akoot.plugins.bluefox.listeners

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.FoxConfig
import co.akoot.plugins.bluefox.extensions.asEmbed
import co.akoot.plugins.bluefox.extensions.names
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.audit.ActionType
import net.dv8tion.jda.api.audit.AuditLogEntry
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.guild.GuildBanEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.awt.Color
import java.time.Duration
import java.time.Instant

class DiscordListener: ListenerAdapter() {
    private val auditLookBack = Duration.ofSeconds(10)
    override fun onGuildMemberRemove(event: GuildMemberRemoveEvent) {
        val user = event.user
        event.guild.retrieveAuditLogs()
            .limit(5)
            .queue { entries ->
                val now = Instant.now()
                val entry = entries.firstOrNull { entry ->
                    entry.targetIdLong == user.idLong &&
                            Duration.between(entry.timeCreated.toInstant(), now) < auditLookBack
                }
                val moderator = entry?.user

                val embed = when (entry?.type) {
                    ActionType.KICK -> {
                        EmbedBuilder().apply {
                            setAuthor("Good riddance, ${user.effectiveName} was kicked!", null, moderator?.effectiveAvatarUrl)
                            setImage(user.effectiveAvatarUrl)
                            setDescription("Thank you ${moderator?.effectiveName ?: "Anonymous"}, I sure hope @${user.name} learns their lesson!")
                            setFooter(entry.reason?.let { "\"$it\" - @${moderator?.name ?: "anonymous"}" })
                            setColor(0xfc6305)
                        }.build()

                    }
                    ActionType.BAN -> {
                        EmbedBuilder().apply {
                            setAuthor("And stay out! ${user.effectiveName} was BANNED!", null, moderator?.effectiveAvatarUrl)
                            setImage(user.effectiveAvatarUrl)
                            setDescription("Thank you ${moderator?.effectiveName ?: "Anonymous"}, may that vagabond @${user.name} find a dumpster to lay in, away from here!")
                            setFooter(entry.reason?.let { "\"$it\" - @${moderator?.name ?: "anonymous"}" })
                            setColor(0xf22602)
                        }.build()
                    }
                    else -> {
                        event.user.asEmbed("This is so sad, ${user.effectiveName} has left us...", "Rest in peace, @${user.name}. *You may be missed...*", smallPic = false, color = Color(0x487587))
                    }
                }
                BlueFox.sendEmbed(embed, channel = "logs")
            }
    }

    override fun onGuildBan(event: GuildBanEvent) {
    }
}