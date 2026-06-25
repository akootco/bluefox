package co.akoot.plugins.bluefox.api.delegating

import co.akoot.plugins.bluefox.api.FoxConfig
import co.akoot.plugins.bluefox.extensions.getMeta
import co.akoot.plugins.bluefox.extensions.setMeta
import org.bukkit.metadata.Metadatable
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.plugin.Plugin
import kotlin.reflect.KType

class MetaBacking(private val backing: Metadatable): DelegateBacking {
    override fun <T> get(plugin: Plugin, key: String, type: KType?): T? {
        return backing.getMeta(key, plugin)
    }

    override fun <T> set(plugin: Plugin, key: String, value: T?) {
        backing.setMeta(key, value, plugin)
    }

    override fun getList(plugin: Plugin, key: String, type: KType?): List<*>? {
        return backing.getMeta(key, plugin)
    }

    override fun setList(
        plugin: Plugin,
        key: String,
        list: List<*>,
        elementType: KType?
    ) {
        backing.setMeta(key, list, plugin)
    }

    override fun getRoot(): String = "${toString()}.meta:"
}

infix fun <T> Metadatable.default(default: T? = null): Delegate<T> = Delegate(this, default)
infix fun <T> Metadatable.of(transform: (String) -> T): Delegate<T> = Delegate(MetaBacking(this), null, fromString = transform)
infix fun <T> Metadatable.from(parent: String): Delegate<T> = Delegate(MetaBacking(this), parent = parent)