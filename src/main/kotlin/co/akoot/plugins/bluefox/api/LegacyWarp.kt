package co.akoot.plugins.bluefox.api

import co.akoot.plugins.bluefox.extensions.xyz
import org.bukkit.Location

data class LegacyWarp(val name: String, val location: Location) {
    override fun toString(): String {
        return "$name: ${location.xyz.toString(", ")}"
    }
}