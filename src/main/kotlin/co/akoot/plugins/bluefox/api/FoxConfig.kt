package co.akoot.plugins.bluefox.api

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions
import com.typesafe.config.ConfigValueFactory
import java.io.File

class FoxConfig(private val file: File) {

    private var config = ConfigFactory.parseFile(file)
    private val options = ConfigRenderOptions.concise().setFormatted(true)
    var autoload = true
    var autosave = true

    /**
     * Loads the config file into memory
     */
    private fun load() {
        config = ConfigFactory.parseFile(file)
    }

    /**
     * Unloads the config file by setting it to an empty config
     */
    fun unload() {
        config = ConfigFactory.empty()
    }

    /**
     * Loads the config file into memory
     * (Semantics WIN!)
     */
    fun reload() {
        load()
    }

    /**
     * Saves the config in memory to the config file
     */
    fun save() {
        file.writeText(config.root().render(options))
    }

    fun set(path: String, value: Any?) {
        config = config.withValue(path, ConfigValueFactory.fromAnyRef(value))
        if (autosave) save()
    }

    // Generic function for single values
    private inline fun <reified T> get(path: String, getter: (Config, String) -> T): T? {
        if(autoload) load()
        return runCatching { getter(config, path) }.getOrNull()
    }

    // Generic function for list values
    private inline fun <reified T> getList(path: String, getter: (Config, String) -> List<T>): List<T> {
        if(autoload) load()
        return runCatching { getter(config, path) }.getOrDefault(emptyList())
    }

    // Getters for Enums
    fun <E: Enum<E>> getEnum(enumClass: Class<E>, path: String): E? {
        if(autoload) load()
        return runCatching { config.getEnum(enumClass, path)}.getOrNull()
    }
    fun <E: Enum<E>> getEnumList(enumClass: Class<E>, path: String): List<E> {
        if(autoload) load()
        return runCatching { config.getEnumList(enumClass, path)}.getOrNull() ?: mutableListOf()
    }

    // Getters for everything else
    fun getString(path: String) = get(path, Config::getString)
    fun getStringList(path: String) = getList(path, Config::getStringList)

    fun getLong(path: String) = get(path, Config::getLong)
    fun getLongList(path: String) = getList(path, Config::getLongList)

    fun getInt(path: String) = get(path, Config::getInt)
    fun getIntList(path: String) = getList(path, Config::getIntList)

    fun getDouble(path: String) = get(path, Config::getDouble)
    fun getDoubleList(path: String) = getList(path, Config::getDoubleList)

    fun getBoolean(path: String) = get(path, Config::getBoolean)
    fun getBooleanList(path: String) = getList(path, Config::getBooleanList)

    fun increment(path: String, amount: Long = 1, max: Long = Long.MAX_VALUE) {
        val value = getLong(path) ?: 0
        set(path, (value + amount).coerceAtMost(max))
    }

    fun increment(path: String, amount: Int = 1, max: Int = Int.MAX_VALUE) {
        val value = getInt(path) ?: 0
        set(path, (value + amount).coerceAtMost(max))
    }

    fun increment(path: String, amount: Double = .01, max: Double = Double.MAX_VALUE) {
        val value = getDouble(path) ?: 0.0
        set(path, (value + amount).coerceAtMost(max))
    }

    fun decrement(path: String, amount: Long = 1, min: Long = 0) {
        val value = getLong(path) ?: 0
        set(path, (value - amount).coerceAtLeast(min))
    }

    fun decrement(path: String, amount: Int = 1, min: Int = 0) {
        val value = getInt(path) ?: 0
        set(path, (value - amount).coerceAtLeast(min))
    }

    fun decrement(path: String, amount: Double = .01, min: Double = 0.0) {
        val value = getDouble(path) ?: 0.0
        set(path, (value - amount).coerceAtLeast(min))
    }
}
