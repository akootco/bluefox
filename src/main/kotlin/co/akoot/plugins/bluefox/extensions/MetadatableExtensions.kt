package co.akoot.plugins.bluefox.extensions

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.delegating.Delegate
import org.bukkit.NamespacedKey
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.Metadatable
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

fun Metadatable.hasMeta(key: String): Boolean = this.hasMetadata(key)

fun Metadatable.removeMeta(key: String, plugin: Plugin = BlueFox.instance) =
    this.removeMetadata(key, plugin)

inline fun <reified T: Any> Metadatable.removeIfNull(key: String, value: T?, plugin: Plugin = BlueFox.instance): T? {
    if(value == null) removeMetadata(key, plugin)
    return value
}

inline fun <reified T : Any>  Metadatable.setMeta(key: String, value: T?, plugin: Plugin = BlueFox.instance) {
    removeIfNull(key, value) ?: return
    setMetadata(key, FixedMetadataValue(plugin, value))
}

inline fun <reified T : Any>  Metadatable.getMeta(key: String, plugin: Plugin = BlueFox.instance): T? {
    return getMetadata(key)
        .firstOrNull { it.owningPlugin == plugin }
        ?.value() as? T?
}

fun <T> Metadatable.delegate(default: T? = null) =  Delegate<T>(this)

