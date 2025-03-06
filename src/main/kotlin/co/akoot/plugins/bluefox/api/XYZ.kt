package co.akoot.plugins.bluefox.api

import co.akoot.plugins.bluefox.util.Text
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.World

class XYZ(val x: Double, val y: Double, val z: Double) {

    companion object {
        val ZERO = XYZ(0.0, 0.0, 0.0)
    }

    constructor(location: Location) : this(location.x, location.y, location.z)

    constructor(x: String, y: String, z: String):
            this(x.toDoubleOrNull() ?: 0.0, y.toDoubleOrNull() ?: 0.0, z.toDoubleOrNull() ?: 0.0)

    constructor(args: Array<out String>):
            this(args[0], args[1], args[2])

    constructor(args: List<String>):
            this(args[0], args[1], args[2])

    constructor(xyz: Double): this(xyz, xyz, xyz)
    constructor(x: Double, z: Double): this(x, 0.0, z)

    val isZero: Boolean get() = x == 0.0 && y == 0.0 && z == 0.0

    fun ifZero(location: Location): XYZ {
        if (isZero) return XYZ(location.x, location.y, location.z)
        return this
    }

    fun toLocation(world: World): Location {
        return Location(world, x, y, z)
    }

    override fun toString(): String {
        return toString(", ")
    }

    fun toIntArray(): IntArray {
        return intArrayOf(x.toInt(), y.toInt(), z.toInt())
    }

    fun toStringList(trailingZeros: Boolean = false): List<String> {
        return if(trailingZeros) doubleArrayOf(x, y, z).map { it.toString() }
        else toIntArray().map { it.toString() }
    }

    fun toString(separator: String = ", ", prefix: String = "", postfix: String = "", trailingZeros: Boolean = false): String {
        return toStringList(trailingZeros).joinToString(separator, prefix, postfix)
    }

    fun toComponent(separator: String = ", ", kolor: Kolor = Kolor.ACCENT, textKolor: Kolor = Kolor.TEXT, prefix: String = "", postfix: String = "", trailingZeros: Boolean = false, bedrock: Boolean = false, rawColor: Boolean = false): Component {
        return Text.list(toStringList(trailingZeros), separator, kolor, textKolor, prefix, postfix, bedrock, rawColor).component
    }
}