package co.akoot.plugins.bluefox.api

import org.bukkit.command.Command
import org.bukkit.plugin.java.JavaPlugin
import java.awt.Color

abstract class FoxPlugin: JavaPlugin() {

    override fun onEnable() {
        // TODO: stuff
        register()
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
}