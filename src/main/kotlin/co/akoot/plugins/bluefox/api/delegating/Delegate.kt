package co.akoot.plugins.bluefox.api.delegating

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.FoxConfig
import org.bukkit.Bukkit
import org.bukkit.metadata.Metadatable
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.plugin.Plugin
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.typeOf

class Delegate<T>(
    private val backend: DelegateBacking,
    private var default: T? = null,
    private var parent: String? = null,
    private val fromString: ((String) -> T)? = null,
    private val toString: ((T) -> String)? = { it.toString() },
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

        val value: T? = if (fromString != null) {
            backend.get<String>(plugin, key, typeOf<String>())?.let(fromString)
        } else {
            val type = property.returnType

            if (type.classifier == List::class) {
                @Suppress("UNCHECKED_CAST")
                backend.getList(
                    plugin,
                    key,
                    type.arguments.firstOrNull()?.type
                ) as T?
            } else {
                backend.get(plugin, key, type)
            }
        } ?: default

        return value
            ?: error("Missing config value for '${backend.getRoot()}$key' and no default provided")
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val plugin = pluginRegistry.getOrPut(property) { property.findPlugin() }
        val key = keyRegistry.getOrPut(property) { property.findKey() }

        if (toString != null) {
            backend.set(plugin, key, toString(value))
            return
        }

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
        annotations.filterIsInstance<Key>().firstOrNull()?.namespace
            ?.let { Bukkit.getServer().pluginManager.getPlugin(it) }
            ?: BlueFox.instance

    private fun KProperty<*>.findKey(): String =
        annotations.filterIsInstance<Key>().firstOrNull()?.path
            ?: "${parent?.let { "$it." } ?: ""}$name"

    infix fun <R> of(fromString: (String) -> R): Delegate<R> =
        Delegate(
            backend = backend,
            parent = parent,
            fromString = fromString
        )

    infix fun <R> from(toString: (R) -> String): Delegate<R> =
        Delegate(
            backend = backend,
            toString = toString
        )

    infix fun default(default: T): Delegate<T> = this.apply { this.default = default }

    infix fun from(parent: String): Delegate<T> = this.apply { this.parent = parent }
}