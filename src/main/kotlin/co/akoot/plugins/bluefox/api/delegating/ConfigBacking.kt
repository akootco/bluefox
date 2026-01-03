package co.akoot.plugins.bluefox.api.delegating

import co.akoot.plugins.bluefox.api.FoxConfig
import org.bukkit.plugin.Plugin
import kotlin.reflect.KType

class ConfigBacking(private val backing: FoxConfig): DelegateBacking {
    override fun <T> get(plugin: Plugin, key: String, type: KType?): T? {
        @Suppress("UNCHECKED_CAST")
        return when (type?.classifier) {
            String::class -> backing.getString(key)
            Int::class -> backing.getInt(key)
            Boolean::class -> backing.getBoolean(key)
            Double::class -> backing.getDouble(key)
            Long::class -> backing.getLong(key)
            else -> null
        } as T?
    }

    override fun <T> set(plugin: Plugin, key: String, value: T?) {
        backing.set(key, value)
    }

    override fun getList(plugin: Plugin, key: String, type: KType?): List<*>?{
        @Suppress("UNCHECKED_CAST")
        return when (type?.classifier) {
            String::class -> backing.getStringList(key)
            Int::class -> backing.getIntList(key)
            Boolean::class -> backing.getBooleanList(key)
            Double::class -> backing.getDoubleList(key)
            Long::class -> backing.getLongList(key)
            else -> null
        } as List<*>?
    }

    override fun setList(
        plugin: Plugin,
        key: String,
        list: List<*>,
        elementType: KType?
    ) {
        backing.set(key, list)
    }
}