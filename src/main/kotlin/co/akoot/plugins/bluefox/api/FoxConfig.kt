package co.akoot.plugins.bluefox.api

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.File

class FoxConfig(private val file: File) {

    private var config = ConfigFactory.parseFile(file)

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

    // Generic function for single values
    private inline fun <reified T> get(path: String, getter: (Config, String) -> T): T? {
        load()
        return runCatching { getter(config, path) }.getOrNull()
    }

    // Generic function for list values
    private inline fun <reified T> getList(path: String, getter: (Config, String) -> List<T>): List<T> {
        load()
        return runCatching { getter(config, path) }.getOrDefault(emptyList())
    }

    // Getters for Enums
    fun <E: Enum<E>> getEnum(enumClass: Class<E>, path: String): E? {
        load()
        return runCatching { config.getEnum(enumClass, path)}.getOrNull()
    }
    fun <E: Enum<E>> getEnumList(enumClass: Class<E>, path: String): List<E> {
        load()
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
}
