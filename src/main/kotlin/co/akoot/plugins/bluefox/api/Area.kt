package co.akoot.plugins.bluefox.api

import org.bukkit.Location
import org.bukkit.World
import kotlin.math.max
import kotlin.math.min

class Area(val world: World, val x1: Int, val y1: Int?, val z1: Int, val x2: Int, val y2: Int?, val z2: Int) {
    companion object {
        fun deserialize(world: World, string: String): Area? {
            val v = string.split(",").map { it.trim().toIntOrNull() ?: return null }
            if(v.size == 4) return Area(world, v[0], null,v[1], v[2], null,v[3])
            else if(v.size == 6) return Area(world, v[0], v[1],v[2], v[3], v[4],v[5])
            return null
        }
    }

    constructor(pos1: Location, pos2: Location, cubic: Boolean = false): this(pos1.world, pos1.blockX, if(cubic) pos1.blockY else null, pos1.blockZ, pos2.blockX, if(cubic) pos2.blockY else null, pos2.blockZ)

    val cubic = y1 != null && y2 != null

    fun has(location: Location): Boolean {
        if(location.world != world) return false
        val minX = min(x1, x2)
        val maxX = max(x1, x2)
        val minZ = min(z1, z2)
        val maxZ = max(z1, z2)

        val isInSquare = location.blockX in minX..maxX && location.blockZ in minZ..maxZ

        if(!cubic) return isInSquare

        val minY = min(y1!!, y2!!)
        val maxY = max(y1, y2)

        return isInSquare && location.blockY in minY..maxY
    }

    override fun toString(): String {
        if(!cubic) return "${world.name}:($x1,$z1),($x2,$z2)"
        return "${world.name}:($x1,$y1,$z1),($x2,$y2,$z2)"
    }

    fun serialize(): String {
        if(!cubic) return "$x1,$z1,$x2,$z2"
        return "$x1,$y1,$z1,$x2,$y2,$z2"
    }
}