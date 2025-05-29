package co.akoot.plugins.bluefox.extensions

import co.akoot.plugins.bluefox.BlueFox
import org.bukkit.NamespacedKey
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.Metadatable
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.plugin.java.JavaPlugin

fun Metadatable.hasMeta(key: String): Boolean = this.hasMetadata(key)

fun Metadatable.removeMeta(key: String, plugin: JavaPlugin = BlueFox.instance) =
    this.removeMetadata(key, plugin)

inline fun <reified T: Any> Metadatable.removeIfNull(key: String, value: T?, plugin: JavaPlugin = BlueFox.instance): T? {
    if(value == null) removeMetadata(key, plugin)
    return value
}

inline fun <reified T : Any>  Metadatable.setMeta(key: String, value: T?, plugin: JavaPlugin = BlueFox.instance) {
    removeIfNull(key, value) ?: return
    setMetadata(key, FixedMetadataValue(plugin, value))
}

inline fun <reified T : Any>  Metadatable.getMeta(key: String, plugin: JavaPlugin = BlueFox.instance): T? {
    return getMetadata(key)
        .firstOrNull { it.owningPlugin == plugin }
        ?.value() as? T?
}
