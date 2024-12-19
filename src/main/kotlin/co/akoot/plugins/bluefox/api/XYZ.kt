package co.akoot.plugins.bluefox.api

import org.bukkit.Location

class XYZ(val x: Double, val y: Double, val z: Double) {

    companion object {
        val ZERO = XYZ(0.0, 0.0, 0.0)
    }

    constructor(x: String, y: String, z: String):
            this(x.toDoubleOrNull() ?: 0.0, y.toDoubleOrNull() ?: 0.0, z.toDoubleOrNull() ?: 0.0)

    constructor(args: Array<out String>):
            this(args[0], args[1], args[2])

    constructor(args: List<String>):
            this(args[0], args[1], args[2])

    constructor(xyz: Double): this(xyz, xyz, xyz)
    constructor(x: Double, z: Double): this(x, 0.0, z)

    fun isZero(): Boolean {
        return x == 0.0 && y == 0.0 && z == 0.0
    }

    fun ifZero(location: Location): XYZ {
        if (isZero()) return XYZ(location.x, location.y, location.z)
        return this
    }
}