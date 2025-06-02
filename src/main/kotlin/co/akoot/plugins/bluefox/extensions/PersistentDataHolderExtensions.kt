package co.akoot.plugins.bluefox.extensions

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.UUID


/**
 * [PersistentDataType] class for primitive data types.
 *
 * **Only use primitive data types, there are no checks, you have to promise me!**
 */
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

/**
 * [PersistentDataType] class for [Location]
 */
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

/**
 * [PersistentDataType] class for [UUID]
 *
 * Stolen from [Paper Docs](https://docs.papermc.io/paper/dev/pdc#custom-data-types)
 */
class UUIDDataType(): PersistentDataType<ByteArray, UUID> {
    override fun getPrimitiveType(): Class<ByteArray> {
        return ByteArray::class.java
    }

    override fun getComplexType(): Class<UUID> {
        return UUID::class.java
    }

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): UUID {
        val bb = ByteBuffer.wrap(primitive)
        val firstLong = bb.getLong()
        val secondLong = bb.getLong()
        return UUID(firstLong, secondLong)
    }

    override fun toPrimitive(complex: UUID, context: PersistentDataAdapterContext): ByteArray {
        val bb = ByteBuffer.allocate(java.lang.Long.BYTES * 2)
        bb.putLong(complex.mostSignificantBits)
        bb.putLong(complex.leastSignificantBits)
        return bb.array()
    }
}

fun PersistentDataHolder.hasPDC(key: NamespacedKey): Boolean {
    return persistentDataContainer.has(key)
}

/**
 * Removes the value of [key] if [value] is null, then returns [value].
 *
 * Useful for checking if [value] is null, then returning after [key] is removed from this [PersistentDataContainer]
 * @param key The [NamespacedKey] of the value to remove from this [PersistentDataContainer]
 * @param value The [Object] to check and return
 * @return [value]
 */
inline fun <reified T: Any> PersistentDataHolder.removeIfNull(key: NamespacedKey, value: T?): T? {
    if(value == null) removePDC(key)
    return value
}

/**
 * Removes the value of [key] if [value] is null, then returns [value].
 *
 * Useful for checking if [value] is null, then returning after [key] is removed from this [PersistentDataContainer]
 * @param key The [NamespacedKey] of the value to remove from this [PersistentDataContainer]
 * @param value The [Object] to check and return
 * @return [value]
 */
inline fun <reified T: Any> PersistentDataHolder.removeIfNull(key: NamespacedKey, value: List<T>?): List<T>? {
    if(value == null) removePDC(key)
    return value
}

/**
 * Get the [Object] value of type [T] from the [PersistentDataContainer] with key [key]
 * @param key The [NamespacedKey] of the value to get from this [PersistentDataContainer]
 */
inline fun <reified T : Any> PersistentDataHolder.getPDC(key: NamespacedKey): T? {
    when(T::class) {
        Location::class -> return persistentDataContainer.get(key, LocationDataType()) as T?
        UUID::class -> return persistentDataContainer.get(key, UUIDDataType()) as T?
        Boolean::class -> return persistentDataContainer.get(key, PersistentDataType.BOOLEAN) as T?
    }
    return persistentDataContainer.get(key, PrimitiveType(T::class.java))
}

/**
 * Get the [List] value of [key] from the [PersistentDataContainer]
 * @param key The [NamespacedKey] of the value to get from this [PersistentDataContainer]
 * @return The [List]
 */
@Suppress("UNCHECKED_CAST") // pesky pesky...
inline fun <reified T : Any> PersistentDataHolder.getPDCList(key: NamespacedKey): List<T>? {
    val dataType = when(T::class) {
        Location::class -> LocationDataType()
        UUID::class -> UUIDDataType()
        Boolean::class -> PersistentDataType.BOOLEAN
        else -> PrimitiveType(T::class.java)
    }
    return persistentDataContainer.get(key, PersistentDataType.LIST.listTypeFrom(dataType)) as List<T>?
}

/**
 * Sets the value of [key] to [value] in the [PersistentDataContainer].
 *
 * **Only supports primitive values and [Location]**
 * @param key The [NamespacedKey] of the value to set in this [PersistentDataContainer]
 * @param value The [Object] to set
 */
inline fun <reified T : Any> PersistentDataHolder.setPDC(key: NamespacedKey, value: T?) {
    val v: T = removeIfNull(key, value) ?: return
    return when(T::class) {
        Location::class -> persistentDataContainer.set(key, LocationDataType(), v as Location)
        UUID::class -> persistentDataContainer.set(key, UUIDDataType(), v as UUID)
        Boolean::class -> persistentDataContainer.set(key, PersistentDataType.BOOLEAN, v as Boolean)
        else -> persistentDataContainer.set(key, PrimitiveType(T::class.java), v)
    }
}

/**
 * Sets the value of [key] to list [value]. If [value] is null then the key is removed from the [PersistentDataContainer]
 * @param key The [NamespacedKey] of the value to set in this [PersistentDataContainer]
 * @param value The [List] to set
 */
@Suppress("UNCHECKED_CAST") // pesky pesky...
inline fun <reified T : Any> PersistentDataHolder.setPDC(key: NamespacedKey, value: List<T>?) {
    val v: List<T> = removeIfNull(key, value) ?: return
    when(T::class) {
        Location::class -> persistentDataContainer.set(key, PersistentDataType.LIST.listTypeFrom(LocationDataType()), v as List<Location>)
        UUID::class -> persistentDataContainer.set(key, PersistentDataType.LIST.listTypeFrom(UUIDDataType()), v as List<UUID>)
        Boolean::class -> persistentDataContainer.set(key, PersistentDataType.LIST.listTypeFrom(PersistentDataType.BOOLEAN), v as List<Boolean>)
        else -> persistentDataContainer.set(key, PersistentDataType.LIST.listTypeFrom(PrimitiveType(T::class.java)), v)
    }
}

/**
 * Removes a value from a [PersistentDataContainer] [List]
 * @param key The [NamespacedKey] to remove from
 * @param value The [Object] to remove from the [PersistentDataContainer]
 * @return If the value was removed successfully
 */
inline fun <reified T : Any>  PersistentDataHolder.removeFromPDCList(key: NamespacedKey, value: T): Boolean {
    val x = getPDCList<T>(key)
    val list = x?.toMutableSet() ?: return false
    val success = list.remove(value)
    setPDC(key, list.toList())
    return success
}

/**
 * Adds a value to a [PersistentDataContainer] [List]
 * @param key The [NamespacedKey] to remove from
 * @param value The [Object] to remove from the [PersistentDataContainer]
 * @return If the value was added successfully
 */
inline fun <reified T : Any>  PersistentDataHolder.addToPDCList(key: NamespacedKey, value: T): Boolean {
    val list = getPDCList<T>(key)?.toMutableSet() ?: mutableSetOf()
    val success = list.add(value)
    setPDC(key, list.toList())
    return success
}

/**
 * Removes [key] from the [PersistentDataContainer]
 * @param key The [NamespacedKey] of the value in the [PersistentDataContainer] to remove
 */
fun PersistentDataHolder.removePDC(key: NamespacedKey) {
    persistentDataContainer.remove(key)
}

/**
 * Convert this [Location] into a [ByteArray]
 * @return This [Location] as a [ByteArray]
 */
fun Location.getBytes(): ByteArray {
    val worldBytes = world.name.toByteArray(StandardCharsets.UTF_8)
    val buffer = ByteBuffer.allocate(
        4 + worldBytes.size + 8 + 8 + 8 + 4 + 4
    )

    buffer.putInt(worldBytes.size) // Store the length of the world name
    buffer.put(worldBytes) // Store the world name bytes
    buffer.putDouble(x)
    buffer.putDouble(y)
    buffer.putDouble(z)
    buffer.putFloat(yaw)
    buffer.putFloat(pitch)

    return buffer.array()
}

/**
 * Converts a [ByteArray] into a [Location] object
 * @param bytes The [ByteArray] to convert
 * @return The [Location] object
 */
fun getLocation(bytes: ByteArray): Location {
    val buffer = ByteBuffer.wrap(bytes)

    val worldSize = buffer.int // Read the world name size
    val worldBytes = ByteArray(worldSize)
    buffer.get(worldBytes) // Read the world name bytes
    val world = String(worldBytes, StandardCharsets.UTF_8)

    val x = buffer.double
    val y = buffer.double
    val z = buffer.double
    val yaw = buffer.float
    val pitch = buffer.float

    return Location(Bukkit.getWorld(world), x, y, z, yaw, pitch)
}

/**
 * Generates a new [NamespacedKey] with the same key as this one but with a different value ([newValue])
 * @param newValue The new value to set to this key (a new, different [NamespacedKey] is created)
 */
fun NamespacedKey.value(newValue: String): NamespacedKey {
    return NamespacedKey(key, newValue)
}

/**
 * Appends [value] to the existing value of this [NamespacedKey]
 *
 * **Example:**
 *
 * `NamespacedKey(plugin, "hello") + "world"` results in
 * `NamespacedKey(plugin, "hello.world")`
 */
operator fun NamespacedKey.plus(value: String): NamespacedKey {
    return value("${value()}.$value")
}
