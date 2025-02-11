package co.akoot.plugins.bluefox.extensions

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.Server
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

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

class LocationDataType(): PersistentDataType<ByteArray, Location> {
    override fun getPrimitiveType(): Class<ByteArray> {
        return ByteArray::class.java
    }

    override fun getComplexType(): Class<Location> {
        return Location::class.java
    }

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): Location {
        return getLocation(primitive)
    }

    override fun toPrimitive(complex: Location, context: PersistentDataAdapterContext): ByteArray {
        return complex.getBytes()
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
        Location::class -> return persistentDataContainer.get(key, LocationDataType()) as T?
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
            persistentDataContainer.set(key, LocationDataType(), value as Location)
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

fun Location.getBytes(): ByteArray {
    val worldBytes = world.name.toByteArray(StandardCharsets.UTF_8)
    val buffer = ByteBuffer.allocate(
        4 + worldBytes.size + 8 + 8 + 8 + 4 + 4
    )

    buffer.putInt(worldBytes.size) // Store the length of the world string
    buffer.put(worldBytes) // Store the world string bytes
    buffer.putDouble(x)
    buffer.putDouble(y)
    buffer.putDouble(z)
    buffer.putFloat(yaw)
    buffer.putFloat(pitch)

    return buffer.array()
}

fun getLocation(bytes: ByteArray): Location {
    val buffer = ByteBuffer.wrap(bytes)

    val worldSize = buffer.int // Read the world string size
    val worldBytes = ByteArray(worldSize)
    buffer.get(worldBytes) // Read the world string bytes
    val world = String(worldBytes, StandardCharsets.UTF_8)

    val x = buffer.double
    val y = buffer.double
    val z = buffer.double
    val yaw = buffer.float
    val pitch = buffer.float

    return Location(Bukkit.getWorld(world), x, y, z, yaw, pitch)
}

fun NamespacedKey.value(newValue: String): NamespacedKey {
    return NamespacedKey(key, newValue)
}

operator fun NamespacedKey.plus(value: String): NamespacedKey {
    return value("${value()}.$value")
}
