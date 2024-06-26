package co.akoot.plugins.bluefox

import co.akoot.plugins.bluefox.api.FoxPlugin
import org.bukkit.plugin.java.JavaPlugin
import java.awt.Color
import java.sql.Connection
import java.sql.DriverManager

class BlueFox : FoxPlugin() {

    lateinit var connection: Connection

    val monthColor = Color.CYAN

    override fun register() {
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
}