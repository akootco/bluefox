package co.akoot.plugins.bluefox.extensions

import co.akoot.plugins.bluefox.api.Kolor
import co.akoot.plugins.bluefox.util.Text
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.World

val World.legacyName: String get() {
    return when(name) {
        "world_nether" -> "nether"
        "world_the_ender" -> "end"
        else -> name
    }
}

fun World.text(color: TextColor = Kolor.TEXT.accent): Text {
    val envColor = when (environment) {
        World.Environment.NETHER -> TextColor.color(0xff0000)
        World.Environment.THE_END -> TextColor.color(0xff00ff)
        World.Environment.NORMAL -> TextColor.color(0x00ff00)
        else -> TextColor.color(0x000000)
    }
    return Text(name, color.mix(envColor))
}