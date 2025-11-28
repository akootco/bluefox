package co.akoot.plugins.bluefox.api

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.extensions.legacyName
import co.akoot.plugins.bluefox.extensions.xyz
import org.bukkit.Bukkit
import org.bukkit.Location

class LegacyHome(name: String, location: Location): LegacyWarp(name, location) {

    companion object {
        fun from(string: String): LegacyHome? {
            val parts = string.split(';')
            val name = parts.getOrNull(0) ?: return null
            val world = parts.getOrNull(1)?.let { BlueFox.getLegacyWorld(it) } ?: return null
            val location = parts.getOrNull(2)?.split(",") ?: return null
            val x = location.getOrNull(0)?.toDoubleOrNull() ?: return null
            val y = location.getOrNull(1)?.toDoubleOrNull() ?: return null
            val z = location.getOrNull(2)?.toDoubleOrNull() ?: return null
            val facing = parts.getOrNull(3)?.split(",") ?: return null
            val yaw = facing.getOrNull(0)?.toFloatOrNull() ?: return null
            val pitch = facing.getOrNull(1)?.toFloatOrNull() ?: return null
            return LegacyHome(name, Location(world, x, y, z, yaw, pitch))
        }
    }
    override fun toString(): String {
        return "$name;${location.world.legacyName};${location.x},${location.y},${location.z};${location.yaw},${location.pitch}"
    }
}