package co.akoot.plugins.bluefox

import org.bukkit.plugin.java.JavaPlugin

class BlueFox : JavaPlugin() {

    override fun onEnable() {
        logger.info("Good day!")
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}