package co.akoot.plugins.bluefox.api

import org.bukkit.Location
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.util.Vector

data class Bounds(val min: Location, val max: Location) : ConfigurationSerializable {
    fun contains(location: Location, padding: Vector = Vector(0, 0, 0), ignoreY: Boolean = false): Boolean {
        return location.world == min.world && location.world == max.world &&
                location.x in (min.x - padding.x)..(max.x + padding.x) &&
                (ignoreY || location.y in (min.y - padding.y)..(max.y + padding.y)) &&
                location.z in (min.z - padding.z)..(max.z + padding.z)
    }

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf("min" to min, "max" to max)
    }

    fun getRandomLocation(): Location {
        val world = min.world
        val x = min.x + (Math.random() * max.x) - min.x
        val y = min.y + (Math.random() * max.y) - min.y
        val z = min.z + (Math.random() * max.z) - min.z
        return Location(world, x, y, z)
    }
}