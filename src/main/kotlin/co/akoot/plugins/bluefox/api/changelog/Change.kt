package co.akoot.plugins.bluefox.api.changelog

import co.akoot.plugins.bluefox.util.getColor
import co.akoot.plugins.bluefox.util.parse
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class Change(val type: Type, val message: String, val hidden: Boolean = false) {
    enum class Type(val color: String, val symbol: String, val icon: String) {
        ADDITION("#10f22e", "+", ":new:"),
        DELETION("#f21045", "-", ":x:"),
        CHANGE("#10aaf2", "^", ":gear:"),
        FIX("#fcd467", "*", ":white_check_mark:")
    }

    val component: Component = "${type.symbol} $message".parse(getColor(type.color))

    fun canBeSeenBy(audience: Audience) = !hidden || audience !is Player || audience.hasPermission("changelog.hidden")

}