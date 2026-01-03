package co.akoot.plugins.bluefox.api.delegating

import co.akoot.plugins.bluefox.extensions.getPDC
import co.akoot.plugins.bluefox.extensions.getPDCList
import co.akoot.plugins.bluefox.extensions.setPDC
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.plugin.Plugin
import kotlin.reflect.KType

class PDCBacking(private val backing: PersistentDataHolder): DelegateBacking {

    override fun <T> get(plugin: Plugin, key: String, type: KType?): T? {
        val key = NamespacedKey(plugin, key)
        return backing.getPDC(key)
    }

    override fun <T> set(plugin: Plugin, key: String, value: T?) {
        val key = NamespacedKey(plugin, key)
        backing.setPDC(key, value)
    }

    override fun getList(plugin: Plugin, key: String, type: KType?): List<*>? {
        val namespacedKey = NamespacedKey(plugin, key)

        return when (type?.classifier) {
            String::class -> backing.getPDCList<String>(namespacedKey)
            Int::class -> backing.getPDCList<Int>(namespacedKey)
            Long::class -> backing.getPDCList<Long>(namespacedKey)
            Double::class -> backing.getPDCList<Double>(namespacedKey)
            Boolean::class -> backing.getPDCList<Boolean>(namespacedKey)
            else -> null
        }
    }

    override fun setList(
        plugin: Plugin,
        key: String,
        list: List<*>,
        elementType: KType?
    ) {
        val key = NamespacedKey(plugin, key)
        backing.setPDC(key, list)
    }
}