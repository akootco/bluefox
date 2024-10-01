package co.akoot.plugins.bluefox.api

import co.akoot.plugins.bluefox.util.IOUtil
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.nio.file.Path

abstract class FoxPlugin : JavaPlugin() {

    val configs: MutableMap<String, FoxConfig> = mutableMapOf()

    override fun onEnable() {
        // TODO: stuff
        if(dataFolder.mkdirs()) logger.info("Created data folder")
        register()
        registerConfigs()
        registerCommands()
        registerEvents()
    }

    override fun onDisable() {
        // TODO: Stuff
        unregister()
    }

    abstract fun register()
    abstract fun unregister()
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
        val config = ConfigFactory.parseFile(configFile)
        val foxConfig = FoxConfig(config)
        configs[name] = foxConfig
        return foxConfig
    }
}