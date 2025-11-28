package co.akoot.plugins.bluefox.extensions

import org.bukkit.Bukkit
import org.bukkit.World

val World.legacyName: String get() {
    return when(name) {
        "world_nether" -> "nether"
        "world_the_ender" -> "end"
        else -> name
    }
}