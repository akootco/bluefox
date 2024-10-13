package co.akoot.plugins.bluefox.api

import co.akoot.plugins.bluefox.util.IOUtil
import com.typesafe.config.ConfigFactory
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

abstract class FoxPlugin() : JavaPlugin() {

    protected val configs: MutableMap<String, FoxConfig> = mutableMapOf()

    override fun onEnable() {
        if(dataFolder.mkdirs()) logger.info("Created data folder")
        load()
        registerConfigs()
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

    fun registerCommand(command: FoxCommand) {
        server.commandMap.register(command.name, command)
    }

    fun registerEventListener(listener: Listener) {
        server.pluginManager.registerEvents(listener, this)
    }

    fun registerConfig(name: String, path: String? = null): FoxConfig {
        val configFile = File(dataFolder, path ?: "$name.conf")
        if (!configFile.exists()) {
            if (IOUtil.extractFile(classLoader, configFile.name, configFile.toPath())) {
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
}