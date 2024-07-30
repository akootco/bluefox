package co.akoot.plugins.bluefox

import co.akoot.plugins.bluefox.api.FoxPlugin
import org.bukkit.plugin.java.JavaPlugin
import java.awt.Color
import java.sql.Connection
import java.sql.DriverManager

class BlueFox : FoxPlugin() {


    override fun register() {
        // Done
        logger.info("Good day!")
    }

    override fun unregister() {
        // Plugin shutdown logic
    }
}