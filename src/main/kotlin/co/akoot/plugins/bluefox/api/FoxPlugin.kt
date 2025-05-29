package co.akoot.plugins.bluefox.api

import co.akoot.plugins.bluefox.api.events.FoxEvent
import co.akoot.plugins.bluefox.api.events.FoxEventCancellable
import co.akoot.plugins.bluefox.util.IOUtil
import org.bukkit.NamespacedKey
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

abstract class FoxPlugin(val id: String) : JavaPlugin() {

    protected val configs: MutableMap<String, FoxConfig> = mutableMapOf()
    open lateinit var settings: FoxConfig

    override fun onEnable() {
        if(dataFolder.mkdirs()) logger.info("Created data folder for $id")
        settings = registerConfig("settings")
        registerConfigs()
        load()
        registerCommands()
        registerEvents()
    }

    override fun onDisable() {
        unload()
        unregisterConfigs()
    }

    abstract fun load()
    abstract fun unload()
    open fun registerCommands() {}
    open fun registerEvents() {}
    open fun registerConfigs() {}
    open fun onCrash() {}

    fun registerCommand(command: FoxCommand) {
        server.commandMap.register(id, command)
    }

    fun registerEventListener(listener: Listener) {
        server.pluginManager.registerEvents(listener, this)
    }

    fun unregisterEventListener(event: Event, listener: Listener) {
        event.handlers.unregister(listener)
    }

    fun unregisterEventListener(listener: Listener) {
        HandlerList.unregisterAll(listener)
    }

    fun registerConfig(name: String, path: String? = null): FoxConfig {
        val conf = path ?: "$name.conf"
        val configFile = File(dataFolder, conf)
        if (!configFile.exists()) {
            if (IOUtil.extractFile(classLoader, conf, configFile.toPath())) {
                logger.info("Loaded config '$name' from jar")
            }
        }
        val config = FoxConfig(configFile)
        configs[name] = config
        return config
    }

    open fun unregisterConfigs() {
        configs.values.forEach(FoxConfig::unload)
        configs.clear()
    }

    fun crash() {
        logger.warning("Sorry, but I am crashing the server now!")
        onCrash()
    }

    fun key(key: String): NamespacedKey {
        return NamespacedKey(this, key)
    }
}