package co.akoot.plugins.bluefox.api

import com.typesafe.config.*
import java.io.File
import java.util.UUID
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class CatConfig(var config: Config, var autosave: Boolean = true) {

    constructor(resourceLocation: String) : this(ConfigFactory.load(resourceLocation), false) {
        this.resourceLocation = resourceLocation
    }

    constructor(file: File, autosave: Boolean = true) : this(ConfigFactory.parseFile(file), autosave) {
        this.file = file
    }

    private var file: File? = null
    private var resourceLocation: String? = null
    private val options = ConfigRenderOptions.concise().setFormatted(true).setJson(false)

    fun reload() {
        config = when {
            file != null -> ConfigFactory.parseFile(file)
            resourceLocation != null -> ConfigFactory.load(resourceLocation)
            else -> config
        }
    }

    fun save() {
        file?.writeText(config.root().render(options))
    }

    inline fun <reified T: Any> get(key: String, listType: KClass<*>? = null): T? =
        get(T::class, key, listType) as? T?

    fun get(type: KClass<*>?, key: String, listType: KClass<*>? = null): Any? {
        if(autosave) reload()
        try {
            val isList = type != null && List::class.java.isAssignableFrom(type.java)
            return when {
                type == String::class -> config.getString(key)
                type == Int::class -> config.getInt(key)
                type == Boolean::class -> config.getBoolean(key)
                type == Double::class -> config.getDouble(key)
                type == Long::class -> config.getLong(key)
                type == UUID::class -> config.getString(key).let { UUID.fromString(it) }
                type == File::class -> File(config.getString(key))
                isList -> when (listType) {
                    UUID::class -> config.getStringList(key).map { UUID.fromString(it) }
                    File::class -> config.getStringList(key).map { File(it) }
                    else -> config.getList(key).unwrapped()
                }
                else -> throw IllegalArgumentException("Unsupported type for config delegation: $type")
            }
        } catch(e: Exception) {
            e.printStackTrace()
        } catch(e2: ConfigException.Missing) {}
        return null
    }

    fun set(key: String, value: Any?, listType: KClass<*>? = null) {
        try {
            config = when (value) {
                is String -> config.withValue(key, ConfigValueFactory.fromAnyRef(value))
                is Int -> config.withValue(key, ConfigValueFactory.fromAnyRef(value))
                is Boolean -> config.withValue(key, ConfigValueFactory.fromAnyRef(value))
                is Double -> config.withValue(key, ConfigValueFactory.fromAnyRef(value))
                is Long -> config.withValue(key, ConfigValueFactory.fromAnyRef(value))
                is UUID, is File -> config.withValue(key, ConfigValueFactory.fromAnyRef(value.toString()))
                is List<*> -> when (listType) {
                    UUID::class, File::class -> config.withValue(
                        key,
                        ConfigValueFactory.fromAnyRef(value.map { it.toString() })
                    )
                    else -> config.withValue(key, ConfigValueFactory.fromIterable(value))
                }
                else -> throw IllegalArgumentException("Unsupported type for config delegation")
            }
            if (autosave) save()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    operator fun <T> invoke(parent: String? = null, default: T): ConfigDelegate<T> =
        ConfigDelegate(this, parent, null, default)

    inline operator fun <reified T: Any> invoke(parent: String? = null, default: List<T> = listOf()): ConfigDelegate<List<T>> {
        val type = T::class
        return ConfigDelegate(this, parent, type, default)
    }

    class ConfigDelegate<T>(
        private val cat: CatConfig,
        private val parent: String?,
        private val listType: KClass<*>? = null,
        private val default: T,
    ) : ReadWriteProperty<Any?, T> {

        private fun fullPath(prop: KProperty<*>) = parent?.let { "$it.${prop.name}" } ?: prop.name

        @Suppress("UNCHECKED_CAST")
        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            val key = fullPath(property)
            return cat.get(default!!::class, key, listType) as? T ?: default
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            val key = fullPath(property)
            cat.set(key, value, listType)
        }
    }
}
