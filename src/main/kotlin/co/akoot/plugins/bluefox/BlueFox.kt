package co.akoot.plugins.bluefox

import co.akoot.plugins.bluefox.api.User
import org.bukkit.plugin.java.JavaPlugin

class BlueFox : JavaPlugin() {

    companion object {
        lateinit var instance: BlueFox
    }

    override fun onEnable() {
        instance = this
        logger.info("Good day!")
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    fun getColor(name: String): Int {
        return 0x00ff00 //TODO: Read from config
    }

    fun getUser(searchString: String): User? {
        return null //TODO: Search for the user
    }
}