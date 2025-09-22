package co.akoot.plugins.bluefox.extensions

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import java.awt.Color

val User.names: String get() = "**$effectiveName**" + if (name == effectiveName) "" else " (@${name})"
fun User.asEmbed(title: String, description: String? = null, footer: String? = null, color: Color = Color(0x7289DA), smallPic: Boolean = true): MessageEmbed {
    return EmbedBuilder().apply {
        if(smallPic) setAuthor(title, null,effectiveAvatarUrl) else setImage(effectiveAvatarUrl)
        if(!smallPic) setTitle(title)
        setColor(color)
        setDescription(description)
        setFooter(footer)
    }.build()
}
