package co.akoot.plugins.bluefox

import co.akoot.plugins.bluefox.api.FoxPlugin
import org.bukkit.plugin.java.JavaPlugin
import java.sql.Connection
import java.sql.DriverManager

class BlueFox : FoxPlugin() {

    companion object {
        lateinit var instance: BlueFox
        fun trace(message: String) {
            instance.logger.info(message)
        }
    }

    lateinit var connection: Connection

    override fun register() {
        instance = this

        // Setup MySQL
        Class.forName("com.mysql.cj.jdbc.Driver")
        connection =
            DriverManager.getConnection("jdbc:mysql://u12007_eur8XeXTIo:OE!qNLot%40wKAGZFnItKqQ9WT@mysql-1.us-mia.game.heavynode.net:3306/s12007_data") //TODO: Setup via config

        // Done
        logger.info("Good day!")
    }

    override fun unregister() {
        // Plugin shutdown logic
        connection.close()
    }

    fun getColor(name: String): Int {
        return 0x00ff00 //TODO: Read from config
    }
}