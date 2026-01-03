package co.akoot.plugins.bluefox.api.delegating

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.FoxConfig
import org.bukkit.Bukkit
import org.bukkit.metadata.Metadatable
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.plugin.Plugin
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Delegate<T>(
    private val backend: DelegateBacking,
    private val default: T? = null
) : ReadWriteProperty<Any?, T> {

    constructor(config: FoxConfig, default: T? = null) : this(ConfigBacking(config), default)
    constructor(holder: PersistentDataHolder, default: T? = null) : this(PDCBacking(holder), default)
    constructor(target: Metadatable, default: T? = null) : this(MetaBacking(target), default)

    companion object {
        val pluginRegistry = mutableMapOf<KProperty<*>, Plugin>()
        val keyRegistry = mutableMapOf<KProperty<*>, String>()
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val plugin = pluginRegistry.getOrPut(property) { property.findPlugin() }
        val key = keyRegistry.getOrPut(property) { property.findKey() }
        val type = property.returnType

        val value = if (type.classifier == List::class) {
            @Suppress("UNCHECKED_CAST")
            backend.getList(
                plugin,
                key,
                type.arguments.firstOrNull()?.type
            ) as T?
        } else {
            backend.get(plugin, key, type)
        }

        return value
            ?: default
            ?: error("Missing config value for '$plugin:$key' and no default provided")
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val plugin = pluginRegistry.getOrPut(property) { property.findPlugin() }
        val key = keyRegistry.getOrPut(property) { property.findKey() }

        val type = property.returnType

        if (type.classifier == List::class) {
            @Suppress("UNCHECKED_CAST")
            backend.setList(
                plugin,
                key,
                value as List<*>,
                type.arguments.firstOrNull()?.type
            )
        } else {
            backend.set(plugin, key, value)
        }
    }

    private fun KProperty<*>.findPlugin(): Plugin =
        annotations.filterIsInstance<Key>().firstOrNull()?.namespace?.let { Bukkit.getServer().pluginManager.getPlugin(it) } ?: BlueFox.instance

    private fun KProperty<*>.findKey(): String =
        annotations.filterIsInstance<Key>().firstOrNull()?.path ?: name
}