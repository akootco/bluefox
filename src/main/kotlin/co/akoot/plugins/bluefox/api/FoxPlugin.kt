package co.akoot.plugins.bluefox.api

import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

abstract class FoxPlugin : JavaPlugin() {

    override fun onEnable() {
        // TODO: stuff
        register()
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

    fun registerCommand(command: FoxCommand) {
        server.commandMap.register(command.name, command)
    }

    fun registerEventListener(listener: Listener) {
        server.pluginManager.registerEvents(listener, this)
    }
}