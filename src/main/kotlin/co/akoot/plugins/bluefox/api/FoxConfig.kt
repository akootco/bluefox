package co.akoot.plugins.bluefox.api

import com.typesafe.config.Config

class FoxConfig(private val config: Config) {

    // Generic function for single values
    private inline fun <reified T> get(path: String, getter: (Config, String) -> T): T? {
        return runCatching { getter(config, path) }.getOrNull()
    }

    // Generic function for list values
    private inline fun <reified T> getList(path: String, getter: (Config, String) -> List<T>): List<T> {
        return runCatching { getter(config, path) }.getOrDefault(emptyList())
    }

    fun getString(path: String) = get(path, Config::getString)

    fun getLong(path: String) = get(path, Config::getLong)

    fun getInt(path: String) = get(path, Config::getInt)

    fun getStringList(path: String) = getList(path, Config::getStringList)

    fun getLongList(path: String) = getList(path, Config::getLongList)

    fun getIntList(path: String) = getList(path, Config::getIntList)
}
