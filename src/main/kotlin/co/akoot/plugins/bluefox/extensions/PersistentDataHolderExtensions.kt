package co.akoot.plugins.bluefox.extensions

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType

class PrimitiveType<T : Any>(private val primitiveType: Class<T>): PersistentDataType<T, T> {
    override fun getPrimitiveType(): Class<T> {
        return primitiveType
    }

    override fun getComplexType(): Class<T> {
        return primitiveType
    }

    override fun fromPrimitive(primitive: T, context: PersistentDataAdapterContext): T {
        return primitive
    }

    override fun toPrimitive(complex: T, context: PersistentDataAdapterContext): T {
        return complex
    }
}

inline fun <reified T: Any> PersistentDataHolder.removeIfNull(key: NamespacedKey, value: T?): T? {
    if(value == null) removePDC(key)
    return value
}

inline fun <reified T: Any> PersistentDataHolder.removeIfNull(key: NamespacedKey, value: List<T>?): List<T>? {
    if(value == null) removePDC(key)
    return value
}

inline fun <reified T : Any> PersistentDataHolder.getPDC(key: NamespacedKey): T? {
    when(T::class) {
        Location::class -> return getPDCLocation(key) as T?
    }
    return persistentDataContainer.get(key, PrimitiveType(T::class.java))
}

inline fun <reified T : Any> PersistentDataHolder.getPDCList(key: NamespacedKey): List<T>? {
    return persistentDataContainer.get(key, PersistentDataType.LIST.listTypeFrom(PrimitiveType(T::class.java)))
}

inline fun <reified T : Any> PersistentDataHolder.setPDC(key: NamespacedKey, value: T?) {
    val v: T = removeIfNull(key, value) ?: return
    when(T::class) {
        Location::class -> {
            setPDCLocation(key, value as Location?)
            return
        }
    }
    persistentDataContainer.set(key, PrimitiveType(T::class.java), v)
}

inline fun <reified T : Any> PersistentDataHolder.setPDCList(key: NamespacedKey, value: List<T>?) {
    val v: List<T> = removeIfNull(key, value) ?: return
    persistentDataContainer.set(key, PersistentDataType.LIST.listTypeFrom(PrimitiveType(T::class.java)), v)
}

fun PersistentDataHolder.removePDC(key: NamespacedKey) {
    persistentDataContainer.remove(key)
}

fun PersistentDataHolder.getPDCLocation(key: NamespacedKey): Location? {
    val world = getPDC<String>(key + "world")?.let { Bukkit.getWorld(it) } ?: return null
    val x = getPDC<Double>(key + "x") ?: return null
    val y = getPDC<Double>(key + "y") ?: return null
    val z = getPDC<Double>(key + "z") ?: return null
    val yaw = getPDC<Float>(key + "yaw") ?: return null
    val pitch = getPDC<Float>(key + "pitch") ?: return null
    return Location(world, x, y, z, yaw, pitch)
}

fun PersistentDataHolder.setPDCLocation(key: NamespacedKey, location: Location?) {
    val value: Location = removeIfNull(key, location) ?: return
    setPDC(key + "world", value.world.name)
    setPDC(key + "x", value.x)
    setPDC(key + "y", value.y)
    setPDC(key + "z", value.z)
    setPDC(key + "yaw", value.yaw)
    setPDC(key + "pitch", value.pitch)
}

fun NamespacedKey.value(newValue: String): NamespacedKey {
    return NamespacedKey(key, newValue)
}

operator fun NamespacedKey.plus(value: String): NamespacedKey {
    return value("${value()}.$value")
}
